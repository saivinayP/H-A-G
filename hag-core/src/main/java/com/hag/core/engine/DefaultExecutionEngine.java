package com.hag.core.engine;

import com.hag.core.reporting.engine.EventPublisher;
import com.hag.core.reporting.events.TestFinishedEvent;
import com.hag.core.reporting.events.TestStartedEvent;

import java.util.Objects;

public class DefaultExecutionEngine implements ExecutionEngine {

    private final EventPublisher eventPublisher;

    public DefaultExecutionEngine(EventPublisher eventPublisher) {
        this.eventPublisher = Objects.requireNonNull(eventPublisher, "eventPublisher must not be null");
    }

    @Override
    public void execute(String testName) {
        long startTime = System.currentTimeMillis();

        // Emit TestStarted
        eventPublisher.publish(
                new TestStartedEvent(testName, testName + ".csv", "LOCAL")
        );

        try {
            // ⚠ Placeholder for real execution logic
            // Steps, includes, dispatching will come later

            // Simulate success for now

            eventPublisher.publish(
                    new TestFinishedEvent(testName, "PASSED", startTime)
            );

        } catch (Exception ex) {

            eventPublisher.publish(
                    new TestFinishedEvent(testName, "ERROR", startTime)
            );

            throw ex;
        }
    }
}
