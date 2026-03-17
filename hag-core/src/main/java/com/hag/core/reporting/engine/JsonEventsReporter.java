package com.hag.core.reporting.engine;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hag.core.reporting.events.Event;
import com.hag.core.reporting.events.StepFinishedEvent;
import com.hag.core.reporting.events.StepStartedEvent;
import com.hag.core.reporting.events.TestFinishedEvent;
import com.hag.core.reporting.events.TestStartedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * JsonEventsReporter — streams each HAG lifecycle event as a single JSON line
 * (NDJSON / Newline-Delimited JSON) to a file on disk.
 *
 * <h3>Output format</h3>
 * One JSON object per line, flushed immediately after every event so that the
 * file can be tailed live during a test run:
 * <pre>
 * {"eventType":"TEST_STARTED","testName":"Login","timestamp":1712345678000,...}
 * {"eventType":"STEP_FINISHED","testName":"Login","stepIndex":1,"status":"PASS",...}
 * </pre>
 *
 * <h3>Configuration</h3>
 * The output directory is read from {@code runner.config.yml}:
 * <pre>
 * reporting:
 *   json:
 *     enabled: true
 *     output-dir: TEST_RESULTS/json
 * </pre>
 * If the directory does not exist it will be created automatically.
 *
 * <h3>Thread-safety</h3>
 * All writes are {@code synchronized} on {@code this} so the reporter is safe
 * under TestNG parallel execution.
 */
public class JsonEventsReporter implements ReportEngine {

    private static final Logger LOG = LoggerFactory.getLogger(JsonEventsReporter.class);

    private static final DateTimeFormatter FILE_TIMESTAMP_FMT =
            DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    private final ObjectMapper mapper = new ObjectMapper();
    private final Path outputDir;

    private BufferedWriter writer;

    /**
     * @param outputDir   directory where the NDJSON file will be written (relative or absolute)
     * @param projectRoot project root used to resolve relative paths
     */
    public JsonEventsReporter(String outputDir, String projectRoot) {
        Path dir = Paths.get(outputDir);
        this.outputDir = dir.isAbsolute() ? dir
                : Paths.get(projectRoot).resolve(dir).normalize();
    }

    // ── ReportEngine lifecycle ──────────────────────────────────────────────

    @Override
    public synchronized void startSuite() {
        try {
            Files.createDirectories(outputDir);
            String fileName = "hag-events-" + LocalDateTime.now().format(FILE_TIMESTAMP_FMT) + ".ndjson";
            Path filePath = outputDir.resolve(fileName);
            writer = Files.newBufferedWriter(filePath, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            LOG.info("HAG → JsonEventsReporter → writing to: {}", filePath);
        } catch (IOException e) {
            LOG.error("HAG → JsonEventsReporter → could not open output file: {}", e.getMessage(), e);
        }
    }

    @Override
    public synchronized void onEvent(Event event) {
        if (writer == null) return;
        try {
            Map<String, Object> payload = buildPayload(event);
            writer.write(mapper.writeValueAsString(payload));
            writer.newLine();
            writer.flush();
        } catch (IOException e) {
            LOG.warn("HAG → JsonEventsReporter → failed to write event: {}", e.getMessage());
        }
    }

    @Override
    public synchronized void endSuite() {
        if (writer == null) return;
        try {
            writer.flush();
            writer.close();
            LOG.info("HAG → JsonEventsReporter → NDJSON file closed.");
        } catch (IOException e) {
            LOG.warn("HAG → JsonEventsReporter → error closing writer: {}", e.getMessage());
        } finally {
            writer = null;
        }
    }

    // ── Payload builders ────────────────────────────────────────────────────

    private Map<String, Object> buildPayload(Event event) {
        // Start with common base fields from Event
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("eventType",  event.getEventType().name());
        map.put("testName",   event.getTestName());
        map.put("threadId",   event.getThreadId());
        map.put("timestamp",  event.getTimestamp());
        map.put("eventId",    event.getEventId());

        // Enrich with type-specific fields
        switch (event.getEventType()) {
            case TEST_STARTED -> {
                TestStartedEvent e = (TestStartedEvent) event;
                map.put("testFile",    e.getTestFile());
                map.put("environment", e.getEnvironment());
                map.put("startTime",   e.getStartTime());
            }
            case TEST_FINISHED -> {
                TestFinishedEvent e = (TestFinishedEvent) event;
                map.put("status",     e.getStatus());
                map.put("durationMs", e.getDurationMs());
                map.put("endTime",    e.getEndTime());
            }
            case STEP_STARTED -> {
                StepStartedEvent e = (StepStartedEvent) event;
                map.put("stepIndex",  e.getStepIndex());
                map.put("action",     e.getAction());
                map.put("actionCase", e.getActionCase());
                map.put("stepType",   e.getStepType());
                map.put("recipient",  e.getRecipient());
            }
            case STEP_FINISHED, STEP_FAILED -> {
                StepFinishedEvent e = (StepFinishedEvent) event;
                map.put("stepIndex",  e.getStepIndex());
                map.put("status",     e.getStatus());
                map.put("durationMs", e.getDurationMs());
                map.put("startTime",  e.getStartTime());
                map.put("message",    e.getMessage());
            }
            default -> { /* SCREENSHOT_CAPTURED, INCLUDE_EXPANDED, FINALLY_* — base fields sufficient */ }
        }

        return map;
    }
}
