package com.hag.dashboard.run;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;

/**
 * Runs HAG tests by spawning a Maven subprocess and streaming
 * stdout/stderr back to the browser via SSE.
 */
@Service
public class RunService {

    private static final Logger LOG = LoggerFactory.getLogger(RunService.class);

    private final RunRepository repo;
    private final ExecutorService executor = Executors.newCachedThreadPool();

    /** Active SSE emitters keyed by runId */
    private final Map<String, List<SseEmitter>> emitters = new ConcurrentHashMap<>();

    /** Buffered log lines keyed by runId (for late-joining viewers) */
    private final Map<String, List<String>> logBuffers = new ConcurrentHashMap<>();

    @Value("${hag.project-root:..}")
    private String projectRoot;

    @Value("${hag.results-dir:../TEST_RESULTS}")
    private String resultsDir;

    public RunService(RunRepository repo) {
        this.repo = repo;
    }

    // ── Trigger ─────────────────────────────────────────────

    public RunRecord trigger(String suite, String browser, String environment,
                             String mode, String gridUrl, int threadCount,
                             String screenshotLevel, boolean headless,
                             String triggeredBy) {

        String runId = UUID.randomUUID().toString().substring(0, 8);

        RunRecord record = new RunRecord(
                runId, suite, browser, environment,
                mode, gridUrl, threadCount, screenshotLevel,
                headless, triggeredBy
        );
        repo.save(record);

        logBuffers.put(runId, Collections.synchronizedList(new ArrayList<>()));

        executor.submit(() -> executeRun(record));

        return record;
    }

    // ── Maven execution ─────────────────────────────────────

    private void executeRun(RunRecord record) {
        String runId = record.getRunId();
        LOG.info("HAG Dashboard → Run [{}] starting: suite={}", runId, record.getSuite());

        List<String> cmd = buildMavenCommand(record);
        LOG.info("HAG Dashboard → Command: {}", String.join(" ", cmd));

        try {
            ProcessBuilder pb = new ProcessBuilder(cmd);
            pb.directory(Paths.get(projectRoot).toAbsolutePath().normalize().toFile());
            pb.redirectErrorStream(true);

            Process process = pb.start();

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    pushLine(runId, line);
                }
            }

            int exitCode = process.waitFor();
            record.setFinishedAt(LocalDateTime.now());

            if (exitCode == 0) {
                record.setStatus("PASSED");
            } else {
                record.setStatus("FAILED");
            }

            // Try to find the HTML report
            discoverReport(record);

            repo.save(record);
            pushLine(runId, "\n=== Run " + record.getStatus() + " (exit code " + exitCode + ") ===");
            pushLine(runId, "[HAG_COMPLETE]");

            LOG.info("HAG Dashboard → Run [{}] finished: status={}", runId, record.getStatus());

        } catch (Exception e) {
            LOG.error("HAG Dashboard → Run [{}] error: {}", runId, e.getMessage(), e);
            record.setStatus("ERROR");
            record.setFinishedAt(LocalDateTime.now());
            repo.save(record);
            pushLine(runId, "ERROR: " + e.getMessage());
            pushLine(runId, "[HAG_COMPLETE]");
        } finally {
            // Clean up emitters after a delay
            executor.submit(() -> {
                try { Thread.sleep(5000); } catch (InterruptedException ignored) {}
                emitters.remove(runId);
            });
        }
    }

    private List<String> buildMavenCommand(RunRecord record) {
        List<String> cmd = new ArrayList<>();

        // Use mvn.cmd on Windows, mvn on Linux/Mac
        String os = System.getProperty("os.name", "").toLowerCase();
        if (os.contains("win")) {
            cmd.add("cmd.exe");
            cmd.add("/c");
            cmd.add("mvn");
        } else {
            cmd.add("mvn");
        }

        cmd.add("clean");
        cmd.add("test");
        cmd.add("-pl");
        cmd.add("hag-runner");
        cmd.add("-am");
        cmd.add("-Dhag.test.root=" + record.getSuite());
        cmd.add("-Dbrowser=" + record.getBrowser());
        cmd.add("-Dheadless=" + record.isHeadless());
        cmd.add("-Dexecution.mode=" + record.getMode());
        cmd.add("-Dhag.env=" + record.getEnvironment());
        cmd.add("-Dthread.count=" + record.getThreadCount());
        cmd.add("-Dscreenshot.level=" + record.getScreenshotLevel());
        cmd.add("-Dhag.run.name=" + record.getRunId());

        if ("grid".equalsIgnoreCase(record.getMode())
                && record.getGridUrl() != null
                && !record.getGridUrl().isBlank()) {
            cmd.add("-Dgrid.url=" + record.getGridUrl());
        }

        return cmd;
    }

    private void discoverReport(RunRecord record) {
        Path resultsPath = Paths.get(resultsDir).toAbsolutePath().normalize();
        if (!Files.isDirectory(resultsPath)) return;

        try (var stream = Files.walk(resultsPath, 2)) {
            stream.filter(p -> p.toString().endsWith(".html"))
                  .filter(p -> p.getFileName().toString().contains(record.getRunId())
                          || p.getParent().getFileName().toString().contains(record.getRunId()))
                  .findFirst()
                  .ifPresent(p -> record.setReportPath(p.toString()));
        } catch (IOException e) {
            LOG.warn("HAG Dashboard → Could not scan reports dir: {}", e.getMessage());
        }

        // Fallback: find the most recent HTML file
        if (record.getReportPath() == null) {
            try (var stream = Files.walk(resultsPath, 2)) {
                stream.filter(p -> p.toString().endsWith(".html"))
                      .max(Comparator.comparingLong(p -> {
                          try { return Files.getLastModifiedTime(p).toMillis(); }
                          catch (IOException e) { return 0L; }
                      }))
                      .ifPresent(p -> record.setReportPath(p.toString()));
            } catch (IOException ignored) {}
        }
    }

    // ── SSE ─────────────────────────────────────────────────

    public SseEmitter subscribe(String runId) {
        SseEmitter emitter = new SseEmitter(0L); // no timeout

        emitters.computeIfAbsent(runId, k -> new CopyOnWriteArrayList<>()).add(emitter);

        emitter.onCompletion(() -> removeEmitter(runId, emitter));
        emitter.onTimeout(()    -> removeEmitter(runId, emitter));
        emitter.onError(t       -> removeEmitter(runId, emitter));

        // Send buffered lines to late joiners
        List<String> buffer = logBuffers.get(runId);
        if (buffer != null) {
            executor.submit(() -> {
                for (String line : new ArrayList<>(buffer)) {
                    try { emitter.send(SseEmitter.event().data(line)); }
                    catch (IOException e) { break; }
                }
            });
        }

        return emitter;
    }

    private void pushLine(String runId, String line) {
        // Buffer
        List<String> buffer = logBuffers.get(runId);
        if (buffer != null) buffer.add(line);

        // Push to all active emitters
        List<SseEmitter> list = emitters.get(runId);
        if (list == null) return;

        for (SseEmitter emitter : list) {
            try {
                emitter.send(SseEmitter.event().data(line));
            } catch (IOException e) {
                removeEmitter(runId, emitter);
            }
        }
    }

    private void removeEmitter(String runId, SseEmitter emitter) {
        List<SseEmitter> list = emitters.get(runId);
        if (list != null) list.remove(emitter);
    }

    // ── Queries ─────────────────────────────────────────────

    public List<RunRecord> getAllRuns() {
        return repo.findAllByOrderByStartedAtDesc();
    }

    public Optional<RunRecord> getRun(String runId) {
        return repo.findById(runId);
    }

    public List<RunRecord> getRunsByStatus(String status) {
        return repo.findByStatusOrderByStartedAtDesc(status);
    }
}
