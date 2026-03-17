package com.hag.core.reporting.engine;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hag.core.reporting.events.Event;
import com.hag.core.reporting.events.StepFinishedEvent;
import com.hag.core.reporting.events.StepStartedEvent;
import com.hag.core.reporting.events.TestFinishedEvent;
import com.hag.core.reporting.events.TestStartedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * ReportPortalEngine — integrates H-A-G with a self-hosted
 * <a href="https://reportportal.io">Report Portal</a> instance via its REST v1 API.
 *
 * <h3>Zero new dependencies</h3>
 * Uses Java's built-in {@link java.net.http.HttpClient} (Java 11+) and the
 * existing Jackson ObjectMapper.
 *
 * <h3>Non-fatal</h3>
 * HTTP failures are logged as WARN and never propagate to the test run.
 * If the RP server is unreachable, tests continue normally.
 *
 * <h3>Thread-safety</h3>
 * A {@link ThreadLocal} stack tracks the current Allure-style item ID hierarchy
 * (launch → test → step) per thread, enabling correct parallel execution.
 *
 * <h3>Configuration (runner.config.yml)</h3>
 * <pre>
 * reporting:
 *   report-portal:
 *     enabled: false
 *     endpoint:    http://localhost:8080
 *     api-token:   &lt;your-rp-uuid-token&gt;
 *     project:     hag-project
 *     launch-name: ""   # defaults to hag.run.name system property
 * </pre>
 */
public class ReportPortalEngine implements ReportEngine {

    private static final Logger LOG = LoggerFactory.getLogger(ReportPortalEngine.class);

    // ── Config ────────────────────────────────────────────────────────────

    /** Immutable config snapshot passed from FrameworkBootstrap. */
    public record Config(
            String endpoint,
            String apiToken,
            String project,
            String launchName
    ) {}

    // ── State ─────────────────────────────────────────────────────────────

    private final Config     cfg;
    private final ObjectMapper mapper = new ObjectMapper();
    private final HttpClient   http;

    /** Suite-level launch ID returned by RP on POST /launch. */
    private volatile String launchId;

    /**
     * Per-thread stack of RP item IDs:
     *   push on TEST_STARTED / STEP_STARTED
     *   pop  on TEST_FINISHED / STEP_FINISHED
     */
    private final ThreadLocal<Deque<String>> itemStack =
            ThreadLocal.withInitial(ArrayDeque::new);

    public ReportPortalEngine(Config cfg) {
        this.cfg = cfg;
        this.http = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
    }

    // ── ReportEngine lifecycle ─────────────────────────────────────────────

    @Override
    public void startSuite() {
        String name = cfg.launchName().isBlank()
                ? System.getProperty("hag.run.name", "H-A-G Suite")
                : cfg.launchName();

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("name",        name);
        body.put("startTime",   System.currentTimeMillis());
        body.put("mode",        "DEFAULT");
        body.put("description", "H-A-G automated test execution");

        String resp = post("/api/v1/" + cfg.project() + "/launch", body);
        if (resp != null) {
            launchId = extractField(resp, "id");
            LOG.info("HAG → ReportPortal → Launch started: id={}", launchId);
        }
    }

    @Override
    public void onEvent(Event event) {
        if (launchId == null) return;       // RP server never responded at startSuite()

        switch (event.getEventType()) {
            case TEST_STARTED    -> handleTestStarted((TestStartedEvent) event);
            case STEP_STARTED    -> handleStepStarted((StepStartedEvent) event);
            case STEP_FINISHED,
                 STEP_FAILED     -> handleStepFinished((StepFinishedEvent) event);
            case TEST_FINISHED   -> handleTestFinished((TestFinishedEvent) event);
            default              -> { /* SCREENSHOT_CAPTURED, INCLUDE_EXPANDED etc. — ignored */ }
        }
    }

    @Override
    public void endSuite() {
        if (launchId == null) return;

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("endTime", System.currentTimeMillis());
        body.put("status",  "PASSED");

        put("/api/v1/" + cfg.project() + "/launch/" + launchId + "/finish", body);
        LOG.info("HAG → ReportPortal → Launch finished: id={}", launchId);
        launchId = null;
    }

    // ── Event handlers ─────────────────────────────────────────────────────

    private void handleTestStarted(TestStartedEvent event) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("name",       event.getTestName());
        body.put("startTime",  event.getStartTime());
        body.put("launchUuid", launchId);
        body.put("type",       "TEST");

        String resp = post("/api/v1/" + cfg.project() + "/item", body);
        if (resp != null) {
            String id = extractField(resp, "id");
            itemStack.get().push(id);
        }
    }

    private void handleStepStarted(StepStartedEvent event) {
        String parentId = itemStack.get().peek();
        if (parentId == null) return;

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("name",       "Step " + event.getStepIndex() + ": " + event.getAction()
                                + (event.getActionCase() != null ? ":" + event.getActionCase() : ""));
        body.put("startTime",  event.getTimestamp());
        body.put("launchUuid", launchId);
        body.put("type",       "STEP");

        String resp = post("/api/v1/" + cfg.project() + "/item/" + parentId, body);
        if (resp != null) {
            String id = extractField(resp, "id");
            itemStack.get().push(id);
        }
    }

    private void handleStepFinished(StepFinishedEvent event) {
        String stepId = itemStack.get().poll();
        if (stepId == null) return;

        String rpStatus = "PASS".equalsIgnoreCase(event.getStatus()) ? "PASSED" : "FAILED";

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("endTime", event.getStartTime() + event.getDurationMs());
        body.put("status",  rpStatus);
        if (event.getMessage() != null && !event.getMessage().isBlank()) {
            body.put("description", event.getMessage());
        }

        put("/api/v1/" + cfg.project() + "/item/" + stepId, body);
    }

    private void handleTestFinished(TestFinishedEvent event) {
        String testId = itemStack.get().poll();
        if (testId == null) return;

        String rpStatus = "PASS".equalsIgnoreCase(event.getStatus()) ? "PASSED" : "FAILED";

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("endTime", event.getEndTime());
        body.put("status",  rpStatus);

        put("/api/v1/" + cfg.project() + "/item/" + testId, body);
    }

    // ── HTTP helpers ────────────────────────────────────────────────────────

    private String post(String path, Map<String, Object> body) {
        return send("POST", path, body);
    }

    private void put(String path, Map<String, Object> body) {
        send("PUT", path, body);
    }

    private String send(String method, String path, Map<String, Object> body) {
        try {
            String json = mapper.writeValueAsString(body);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(cfg.endpoint() + path))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + cfg.apiToken())
                    .method(method, HttpRequest.BodyPublishers.ofString(json))
                    .timeout(Duration.ofSeconds(5))
                    .build();

            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 300) {
                LOG.warn("HAG → ReportPortal → {} {} returned HTTP {}: {}",
                        method, path, response.statusCode(), response.body());
                return null;
            }
            return response.body();

        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            LOG.warn("HAG → ReportPortal → HTTP call interrupted for {} {}", method, path);
            return null;
        } catch (Exception e) {
            LOG.warn("HAG → ReportPortal → HTTP call failed for {} {}: {}", method, path, e.getMessage());
            return null;
        }
    }

    /**
     * Minimal JSON field extractor — avoids a full parse for simple response bodies like
     * {@code {"id":"abc123","..."}}.
     */
    private String extractField(String json, String field) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = mapper.readValue(json, Map.class);
            Object val = map.get(field);
            return val != null ? val.toString() : UUID.randomUUID().toString();
        } catch (Exception e) {
            LOG.warn("HAG → ReportPortal → could not extract '{}' from JSON response", field);
            return UUID.randomUUID().toString();
        }
    }
}
