package com.hag.core.reporting.events;

import java.util.Objects;

public class StepFinishedEvent extends Event {
    private final int stepIndex;
    private final String status;
    private final long durationMs;
    private final String message;

    public StepFinishedEvent(String testName, int stepIndex, String status, long startTime, long durationMs, String message) {
        super(EventType.STEP_FINISHED, testName);
        this.stepIndex = stepIndex;
        this.status = Objects.requireNonNull(status, "status must not be null");
        this.durationMs = System.currentTimeMillis() - startTime;
        this.message = message;
    }

    public int getStepIndex() {
        return stepIndex;
    }

    public String getStatus() {
        return status;
    }

    public long getDurationMs() {
        return durationMs;
    }

    public String getMessage() {
        return message;
    }
}
