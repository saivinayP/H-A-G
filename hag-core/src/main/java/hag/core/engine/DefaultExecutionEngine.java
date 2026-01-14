package hag.core.engine;

import hag.core.engine.context.ExecutionContext;
import hag.core.engine.context.ResolvedStep;
import hag.core.engine.dispatcher.ActionDispatcher;
import hag.core.engine.dispatcher.ControlActions;
import hag.core.engine.model.Step;
import hag.core.engine.parser.CsvTestParser;
import hag.core.engine.parser.IncludeResolver;
import hag.core.reporting.engine.EventPublisher;
import hag.core.reporting.events.*;
import hag.core.reporting.events.*;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DefaultExecutionEngine implements ExecutionEngine {

    private final EventPublisher eventPublisher;
    private final ActionDispatcher dispatcher;
    private final CsvTestParser parser;
    private final IncludeResolver includeResolver;

    public DefaultExecutionEngine(
            EventPublisher eventPublisher,
            ActionDispatcher dispatcher,
            CsvTestParser parser,
            IncludeResolver includeResolver
    ) {
        this.eventPublisher = Objects.requireNonNull(eventPublisher);
        this.dispatcher = Objects.requireNonNull(dispatcher);
        this.parser = Objects.requireNonNull(parser);
        this.includeResolver = Objects.requireNonNull(includeResolver);
    }

    @Override
    public void execute(String testName, Path testFile) {

        ExecutionContext context = new ExecutionContext();
        long testStartTime = System.currentTimeMillis();

        publishTestStarted(testName, testFile);

        TestOutcome outcome = TestOutcome.PASSED;

        List<Step> allSteps = parseAndPrepareSteps(testName, testFile);

        StepFlowSplitter.Flow flow = StepFlowSplitter.split(allSteps);

        try {
            runSteps(testName, flow.main(), context);
        } catch (Exception ex) {
            outcome = TestOutcome.FAILED;
        } finally {
            runFinallySteps(testName, flow.fin(), context);
            publishTestFinished(testName, outcome, testStartTime);
        }
    }

    /* =======================
       Parsing & Preparation
       ======================= */

    private List<Step> parseAndPrepareSteps(String testName, Path testFile) {
        List<Step> parsedSteps = parser.parse(testFile);
        return expandIncludes(testName, parsedSteps, testFile.getParent());
    }

    private List<Step> expandIncludes(
            String testName,
            List<Step> steps,
            Path baseDir
    ) {
        List<Step> resolved = new ArrayList<>();

        for (Step step : steps) {
            if (ControlActions.INCLUDE.equalsIgnoreCase(step.getAction())) {
                resolved.addAll(
                        includeResolver.resolve(testName, step, baseDir)
                );
            } else {
                resolved.add(step);
            }
        }
        return resolved;
    }

    /* =======================
       Execution
       ======================= */

    private void runSteps(
            String testName,
            List<Step> steps,
            ExecutionContext context
    ) throws Exception {

        for (Step step : steps) {
            executeSingleStep(testName, step, context);
        }
    }

    private void executeSingleStep(
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
            Step resolvedStep = ResolvedStep.resolve(step, context);
            dispatcher.dispatch(resolvedStep, context);

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

    /* =======================
       Finally handling
       ======================= */

    private void runFinallySteps(
            String testName,
            List<Step> finallySteps,
            ExecutionContext context
    ) {

        if (finallySteps.isEmpty()) {
            return;
        }

        eventPublisher.publish(new FinallyStartedEvent(testName));

        for (Step step : finallySteps) {
            try {
                executeSingleStep(testName, step, context);
            } catch (Exception ignored) {
                // Finally must never fail the test
            }
        }

        eventPublisher.publish(new FinallyFinishedEvent(testName));
    }

    /* =======================
       Test lifecycle events
       ======================= */

    private void publishTestStarted(String testName, Path testFile) {
        eventPublisher.publish(
                new TestStartedEvent(
                        testName,
                        testFile.toString(),
                        "LOCAL"
                )
        );
    }

    private void publishTestFinished(
            String testName,
            TestOutcome outcome,
            long testStartTime
    ) {
        eventPublisher.publish(
                new TestFinishedEvent(
                        testName,
                        outcome.name(),
                        testStartTime
                )
        );
    }

    private enum TestOutcome {
        PASSED,
        FAILED
    }
}
