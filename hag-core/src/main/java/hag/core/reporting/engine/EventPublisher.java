package hag.core.reporting.engine;

import hag.core.reporting.events.Event;

public interface EventPublisher {

    void publish(Event event);
}
