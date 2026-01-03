package com.hag.core.engine;

import com.hag.core.engine.context.ExecutionContext;
import com.hag.core.engine.dispatcher.ActionDispatcher;
import com.hag.core.engine.model.Step;
import com.hag.core.engine.parser.CsvTestParser;
import com.hag.core.reporting.engine.EventPublisher;
import com.hag.core.reporting.events.*;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

public class DefaultExecutionEngine implements ExecutionEngine {

    private final EventPublisher eventPublisher;
    private final ActionDispatcher dispatcher;
    private final CsvTestParser parser;

    public DefaultExecutionEngine(
            EventPublisher eventPublisher,
            ActionDispatcher dispatcher,
            CsvTestParser parser
    ) {
        this.eventPublisher = Objects.requireNonNull(eventPublisher);
        this.dispatcher = Objects.requireNonNull(dispatcher);
        this.parser = Objects.requireNonNull(parser);
    }

    @Override
    public void execute(String testName, Path testFile) {

        ExecutionContext context = new ExecutionContext();
        long testStartTime = System.currentTimeMillis();

        eventPublisher.publish(
                new TestStartedEvent(testName, testFile.toString(), "LOCAL")
        );

        List<Step> steps = parser.parse(testFile);

        try {
            for (Step step : steps) {
                executeStep(testName, step, context);
            }

            eventPublisher.publish(
                    new TestFinishedEvent(testName, "PASSED", testStartTime)
            );

        } catch (Exception ex) {

            eventPublisher.publish(
                    new TestFinishedEvent(testName, "FAILED", testStartTime)
            );

            throw new RuntimeException("Test execution failed", ex);
        }
    }

    private void executeStep(
            String testName,
            Step step,
            ExecutionContext context
    ) throws Exception {

        int stepIndex = context.nextStepIndex();
        long stepStartTime = System.currentTimeMillis();

        eventPublisher.publish(
                new StepStartedEvent(
                        testName,
                        stepIndex,
                        step.getAction(),
                        null,
                        "CORE",
                        step.getRecipient()
                )
        );

        try {
            dispatcher.dispatch(step, context);

            eventPublisher.publish(
                    new StepFinishedEvent(
                            testName,
                            stepIndex,
                            "PASSED",
                            stepStartTime,
                            null
                    )
            );

        } catch (Exception ex) {

            eventPublisher.publish(
                    new StepFailedEvent(
                            testName,
                            stepIndex,
                            "EXECUTION",
                            ex.getMessage()
                    )
            );

            eventPublisher.publish(
                    new StepFinishedEvent(
                            testName,
                            stepIndex,
                            "FAILED",
                            stepStartTime,
                            ex.getMessage()
                    )
            );

            throw ex;
        }
    }
}
