package com.hag.core.analytics;

import java.util.List;

public final class ExecutionSummary {

    private final String testName;
    private final long startTime;
    private final long endTime;
    private final int totalSteps;
    private final int passedSteps;
    private final int failedSteps;
    private final long durationMillis;
    private final List<StepMetrics> steps;

    public ExecutionSummary(
            String testName,
            long startTime,
            long endTime,
            int totalSteps,
            int passedSteps,
            int failedSteps,
            long durationMillis,
            List<StepMetrics> steps
    ) {
        this.testName = testName;
        this.startTime = startTime;
        this.endTime = endTime;
        this.totalSteps = totalSteps;
        this.passedSteps = passedSteps;
        this.failedSteps = failedSteps;
        this.durationMillis = durationMillis;
        this.steps = steps;
    }

    public String getTestName() { return testName; }
    public long getStartTime() { return startTime; }
    public long getEndTime() { return endTime; }
    public int getTotalSteps() { return totalSteps; }
    public int getPassedSteps() { return passedSteps; }
    public int getFailedSteps() { return failedSteps; }
    public long getDurationMillis() { return durationMillis; }
    public List<StepMetrics> getSteps() { return steps; }
}