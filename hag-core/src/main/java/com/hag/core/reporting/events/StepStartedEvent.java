package com.hag.core.reporting.events;

import java.util.Objects;

public class StepStartedEvent extends Event {
    private final int stepIndex;
    private final String action;
    private final String actionCase;
    private final String stepType;
    private final String recipient;

    public StepStartedEvent(String testName, int stepIndex, String action, String actionCase, String stepType, String recipient) {
        super(EventType.STEP_STARTED, testName);
        this.stepIndex = stepIndex;
        this.action = Objects.requireNonNull(action, "action must not be null");
        this.actionCase = actionCase;
        this.stepType = Objects.requireNonNull(stepType, "stepType must not be null");
        this.recipient = recipient;
    }

    public int getStepIndex() {
        return stepIndex;
    }

    public String getAction() {
        return action;
    }

    public String getActionCase() {
        return actionCase;
    }

    public String getStepType() {
        return stepType;
    }

    public String getRecipient() {
        return recipient;
    }
}
