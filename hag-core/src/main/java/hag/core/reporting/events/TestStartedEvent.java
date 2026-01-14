package hag.core.reporting.events;

import java.util.Objects;

public class TestStartedEvent extends Event {
    private final long startTime;
    private final String testFile;
    private final String environment;

    public TestStartedEvent(String testName, String testFile, String environment) {
        super(EventType.TEST_STARTED, testName);
        this.startTime = System.currentTimeMillis();
        this.testFile = Objects.requireNonNull(testFile, "testFile cannot be null");
        this.environment = Objects.requireNonNull(environment, "environment cannot be null");
    }

    public long getStartTime() {
        return startTime;
    }

    public String getTestFile() {
        return testFile;
    }

    public String getEnvironment() {
        return environment;
    }
}
