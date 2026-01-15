package com.hag.core.reporting.engine;

import com.hag.core.reporting.events.Event;

public interface EventPublisher {

    void publish(Event event);
}
