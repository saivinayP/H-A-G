package com.hag.core.reporting.events;

import java.util.Objects;

public class IncludeExpandedEvent extends Event {

    private final String includePath;
    private final int expandedStepCount;

    public IncludeExpandedEvent(String testName, String includePath, int expandedStepCount) {
        super(EventType.INCLUDE_EXPANDED, testName);
        this.includePath = Objects.requireNonNull(includePath, "includePath must not be null");
        this.expandedStepCount = expandedStepCount;
    }

    public String getIncludePath() {
        return includePath;
    }

    public int getExpandedStepCount() {
        return expandedStepCount;
    }
}
