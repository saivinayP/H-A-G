package com.hag.core.reporting.events;

import java.util.Objects;

public class TestFinishedEvent extends Event {
    private final long endTime;
    private final String status;
    private final long durationMs;

    public TestFinishedEvent(String testName, String status, long startTime) {
        super(EventType.TEST_FINISHED, testName);
        this.endTime = System.currentTimeMillis();
        this.status = Objects.requireNonNull(status, "status must not be null");
        this.durationMs = endTime - startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public String getStatus() {
        return status;
    }

    public long getDurationMs() {
        return durationMs;
    }
}
