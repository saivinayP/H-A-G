package com.hag.core.reporting.engine;

import com.hag.core.reporting.events.Event;

public interface ReportEngine {

    void startSuite();

    void onEvent(Event event);

    void endSuite();
}
