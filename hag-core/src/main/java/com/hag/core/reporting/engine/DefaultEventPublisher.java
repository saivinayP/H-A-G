package com.hag.core.reporting.engine;

import com.hag.core.reporting.events.Event;

import java.util.List;
import java.util.Objects;

public class DefaultEventPublisher implements EventPublisher {

    private final List<ReportEngine> reportEngines;

    public DefaultEventPublisher(List<ReportEngine> reportEngines) {
        this.reportEngines = Objects.requireNonNull(reportEngines, "reportEngines must not be null");
    }

    @Override
    public void startSuite() {
        for (ReportEngine engine : reportEngines) {
            engine.startSuite();
        }
    }

    @Override
    public void publish(Event event) {
        for (ReportEngine engine : reportEngines) {
            engine.onEvent(event);
        }
    }

    @Override
    public void endSuite() {
        for (ReportEngine engine : reportEngines) {
            engine.endSuite();
        }
    }
}
