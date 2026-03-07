package com.hag.core.analytics;

public final class StepMetrics {

    private final int stepIndex;
    private final String action;
    private final String status;
    private final long startTime;
    private final long durationMillis;
    private final String failureMessage;

    public StepMetrics(
            int stepIndex,
            String action,
            String status,
            long startTime,
            long durationMillis,
            String failureMessage
    ) {
        this.stepIndex = stepIndex;
        this.action = action;
        this.status = status;
        this.startTime = startTime;
        this.durationMillis = durationMillis;
        this.failureMessage = failureMessage;
    }

    public int getStepIndex() { return stepIndex; }
    public String getAction() { return action; }
    public String getStatus() { return status; }
    public long getStartTime() { return startTime; }
    public long getDurationMillis() { return durationMillis; }
    public String getFailureMessage() { return failureMessage; }
}