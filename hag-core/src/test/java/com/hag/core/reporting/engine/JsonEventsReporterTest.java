package com.hag.core.reporting.engine;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hag.core.reporting.events.StepFinishedEvent;
import com.hag.core.reporting.events.TestFinishedEvent;
import com.hag.core.reporting.events.TestStartedEvent;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * Unit tests for {@link JsonEventsReporter}.
 *
 * <p>Each test creates a fresh reporter with a unique temp directory so the
 * tests are fully isolated and can run in any order.
 */
public class JsonEventsReporterTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private Path tempDir;
    private JsonEventsReporter reporter;

    @BeforeMethod
    public void setUp() throws IOException {
        tempDir  = Files.createTempDirectory("hag-json-reporter-test");
        reporter = new JsonEventsReporter(tempDir.toString(), tempDir.toString());
        reporter.startSuite();
    }

    @AfterMethod
    public void tearDown() throws IOException {
        reporter.endSuite();
        // Clean up temp files
        Files.walk(tempDir)
             .sorted(java.util.Comparator.reverseOrder())
             .forEach(p -> { try { Files.deleteIfExists(p); } catch (IOException ignored) {} });
    }

    // ── Helpers ──────────────────────────────────────────────────────────

    /** Returns the single NDJSON file written to {@code tempDir}. */
    private Path ndjsonFile() throws IOException {
        return Files.list(tempDir)
                    .filter(p -> p.toString().endsWith(".ndjson"))
                    .findFirst()
                    .orElseThrow(() -> new AssertionError("No .ndjson file found in " + tempDir));
    }

    /** Reads all lines from the NDJSON file and parses each as a JSON map. */
    private List<Map<String, Object>> readLines() throws IOException {
        return Files.readAllLines(ndjsonFile()).stream()
                .filter(line -> !line.isBlank())
                .map(line -> {
                    try {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> map = MAPPER.readValue(line, Map.class);
                        return map;
                    } catch (Exception e) {
                        throw new AssertionError("Invalid JSON line: " + line, e);
                    }
                })
                .toList();
    }

    // ── Tests ─────────────────────────────────────────────────────────────

    @Test
    public void startSuiteCreatesNdjsonFile() throws IOException {
        // startSuite() is called in setUp() — file must exist already
        Path file = ndjsonFile();
        Assert.assertTrue(Files.exists(file),
                "NDJSON file should be created during startSuite()");
        Assert.assertTrue(file.getFileName().toString().startsWith("hag-events-"),
                "NDJSON file name should start with hag-events-");
    }

    @Test
    public void testStartedEventWrittenAsJsonLine() throws IOException {
        reporter.onEvent(new TestStartedEvent("MyTest", "tests/my_test.csv", "dev"));

        List<Map<String, Object>> lines = readLines();
        Assert.assertEquals(lines.size(), 1, "Should write exactly one line");

        Map<String, Object> payload = lines.get(0);
        Assert.assertEquals(payload.get("eventType"), "TEST_STARTED");
        Assert.assertEquals(payload.get("testName"),  "MyTest");
        Assert.assertNotNull(payload.get("eventId"),  "eventId should be present");
        Assert.assertNotNull(payload.get("timestamp"), "timestamp should be present");
        Assert.assertEquals(payload.get("testFile"),  "tests/my_test.csv");
        Assert.assertEquals(payload.get("environment"), "dev");
    }

    @Test
    public void stepFinishedEventContainsDurationAndStatus() throws IOException {
        reporter.onEvent(new StepFinishedEvent("SomeTest", 3, "PASS", System.currentTimeMillis(), 142L, "Step passed"));

        List<Map<String, Object>> lines = readLines();
        Assert.assertEquals(lines.size(), 1);

        Map<String, Object> payload = lines.get(0);
        Assert.assertEquals(payload.get("eventType"), "STEP_FINISHED");
        Assert.assertEquals(payload.get("stepIndex"), 3);
        Assert.assertEquals(payload.get("status"),    "PASS");
        Assert.assertEquals(payload.get("durationMs"), 142);
        Assert.assertEquals(payload.get("message"),   "Step passed");
    }

    @Test
    public void multipleEventsWriteMultipleLines() throws IOException {
        long startTime = System.currentTimeMillis();
        reporter.onEvent(new TestStartedEvent("T1", "tests/t1.csv", "dev"));
        reporter.onEvent(new StepFinishedEvent("T1", 1, "PASS", startTime, 50L, "ok"));
        reporter.onEvent(new StepFinishedEvent("T1", 2, "FAIL", startTime, 80L, "assertion failed"));
        reporter.onEvent(new TestFinishedEvent("T1", "FAILED", startTime));

        List<Map<String, Object>> lines = readLines();
        Assert.assertEquals(lines.size(), 4, "Should produce exactly 4 lines");
        Assert.assertEquals(lines.get(0).get("eventType"), "TEST_STARTED");
        Assert.assertEquals(lines.get(1).get("eventType"), "STEP_FINISHED");
        Assert.assertEquals(lines.get(2).get("eventType"), "STEP_FINISHED");
        Assert.assertEquals(lines.get(3).get("eventType"), "TEST_FINISHED");
    }

    @Test
    public void endSuiteDoesNotThrowAndFileRemainsReadable() throws IOException {
        reporter.onEvent(new TestStartedEvent("T2", "tests/t2.csv", "qa"));
        // endSuite() called in tearDown(); verify the file can be read before that
        List<Map<String, Object>> lines = readLines();
        Assert.assertFalse(lines.isEmpty());
    }

    @Test
    public void eachLineIsValidJson() throws IOException {
        long t = System.currentTimeMillis();
        reporter.onEvent(new TestStartedEvent("T3", "tests/t3.csv", "prod"));
        reporter.onEvent(new StepFinishedEvent("T3", 1, "PASS", t, 10L, ""));
        reporter.onEvent(new TestFinishedEvent("T3", "PASSED", t));

        // readLines() already forces Jackson parse — if any line is invalid it throws
        List<Map<String, Object>> lines = readLines();
        Assert.assertEquals(lines.size(), 3);
    }
}
