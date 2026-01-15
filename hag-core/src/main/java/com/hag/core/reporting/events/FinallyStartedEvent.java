package com.hag.core.reporting.events;

public class FinallyStartedEvent extends Event {
    public FinallyStartedEvent(String testName) {
        super(EventType.TEST_STARTED, testName);
    }
}
