package com.hag.core.reporting.engine;

import com.hag.core.reporting.events.Event;
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
        if (event instanceof com.hag.core.reporting.events.StepStartedEvent e) {
            String baseAction = e.getAction();
            if (baseAction != null && baseAction.contains("|")) {
                baseAction = baseAction.substring(0, baseAction.indexOf('|'));
            }
            if (baseAction != null && baseAction.contains(":")) {
                baseAction = baseAction.substring(0, baseAction.indexOf(':'));
            }

            if ("SECTION".equalsIgnoreCase(baseAction)) {
                log.info("\n--- {} ---", e.getKey() != null ? e.getKey() : "SECTION");
            } else if ("LOG".equalsIgnoreCase(baseAction)) {
                log.info("ℹ {}", e.getKey() != null ? e.getKey() : "LOG");
            } else {
                log.info("▶ Step {}: {}", e.getStepIndex(), e.getAction());
            }
        } else if (event instanceof com.hag.core.reporting.events.StepFinishedEvent e) {
            if ("WARN".equalsIgnoreCase(e.getStatus())) {
                log.warn("⚠ Step {} WARN: {}", e.getStepIndex(), e.getMessage());
            } else if ("FAILED".equalsIgnoreCase(e.getStatus())) {
                log.error("❌ Step {} FAILED: {}", e.getStepIndex(), e.getMessage());
            }
        } else if (event instanceof com.hag.core.reporting.events.TestStartedEvent e) {
            log.info("Testing Scenario: {}", e.getTestName());
        }
    }

    @Override
    public void endSuite() {
        log.info("=== Test Suite Finished ===");
    }
}
