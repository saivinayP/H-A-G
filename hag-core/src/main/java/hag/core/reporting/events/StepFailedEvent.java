package hag.core.reporting.events;

import java.util.Objects;

public class StepFailedEvent extends Event {
    private final int stepIndex;
    private final String failureType;
    private final String errorMessage;

    public StepFailedEvent(String testName, int stepIndex, String failureType, String errorMessage) {
        super(EventType.STEP_FAILED, testName);
        this.stepIndex = stepIndex;
        this.failureType = Objects.requireNonNull(failureType, "failureType must not be null");
        this.errorMessage = Objects.requireNonNull(errorMessage, "errorMessage must not be null");
    }

    public String getFailureType() {
        return failureType;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public int getStepIndex() {
        return stepIndex;
    }
}
