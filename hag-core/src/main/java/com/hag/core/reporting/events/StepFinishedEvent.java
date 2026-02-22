package com.hag.core.reporting.events;

public class StepFinishedEvent extends Event {

    private final int stepIndex;
    private final String status;
    private final long startTime;
    private final long durationMs;
    private final String message;

    public StepFinishedEvent(
            String testName,
            int stepIndex,
            String status,
            long startTime,
            long durationMs,
            String message
    ) {
        super(EventType.STEP_FINISHED, testName);

        this.stepIndex = stepIndex;
        this.status = status;
        this.startTime = startTime;
        this.durationMs = durationMs;
        this.message = message;
    }

    public int getStepIndex() {
        return stepIndex;
    }

    public String getStatus() {
        return status;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getDurationMs() {
        return durationMs;
    }

    public String getMessage() {
        return message;
    }
}
