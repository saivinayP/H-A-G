package com.hag.core.reporting.events;

public class FinallyFinishedEvent extends Event {
    public FinallyFinishedEvent(String testName) {
        super(EventType.TEST_FINISHED, testName);
    }
}
