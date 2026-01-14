package hag.core.reporting.engine;

import hag.core.reporting.events.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConsoleReportEngine implements ReportEngine {

    private static final Logger log =
            LoggerFactory.getLogger(ConsoleReportEngine.class);

    @Override
    public void startSuite() {
        log.info("=== Test Suite Started ===");
    }

    @Override
    public void onEvent(Event event) {
        log.info(
                "[{}] Test={}, Timestamp={}",
                event.getEventType(),
                event.getTestName(),
                event.getTimestamp()
        );
    }

    @Override
    public void endSuite() {
        log.info("=== Test Suite Finished ===");
    }
}
