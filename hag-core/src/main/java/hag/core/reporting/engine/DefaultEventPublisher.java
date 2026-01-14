package hag.core.reporting.engine;

import hag.core.reporting.events.Event;

import java.util.List;
import java.util.Objects;

public class DefaultEventPublisher implements EventPublisher {

    private final List<ReportEngine> reportEngines;

    public DefaultEventPublisher(List<ReportEngine> reportEngines) {
        this.reportEngines = Objects.requireNonNull(reportEngines, "reportEngines must not be null");
    }

    @Override
    public void publish(Event event) {
        for (ReportEngine engine : reportEngines) {
            engine.onEvent(event);
        }
    }
}
