package com.hag.core.engine;

import com.hag.core.config.FrameworkConfig;
import com.hag.core.context.ExecutionContext;
import com.hag.core.model.Step;
import com.hag.core.resolver.StepResolver;
import com.hag.core.dispatcher.ActionDispatcher;
import com.hag.core.parser.CsvTestParser;
import com.hag.core.parser.IncludeResolver;
import com.hag.core.result.ExecutionResult;
import com.hag.core.reporting.engine.EventPublisher;
import com.hag.core.reporting.events.*;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * DefaultExecutionEngine
 * Core orchestrator of test execution.
 * Pure orchestration — no business logic.
 */
public final class DefaultExecutionEngine implements ExecutionEngine {

    private final EventPublisher eventPublisher;
    private final ActionDispatcher dispatcher;
    private final CsvTestParser parser;
    private final IncludeResolver includeResolver;
    private final FailureArtifactProvider artifactProvider;
    private final FrameworkConfig config;

    public DefaultExecutionEngine(
            EventPublisher eventPublisher,
            ActionDispatcher dispatcher,
            CsvTestParser parser,
            IncludeResolver includeResolver,
            FailureArtifactProvider artifactProvider,
            FrameworkConfig config
    ) {
        this.eventPublisher = Objects.requireNonNull(eventPublisher);
        this.dispatcher = Objects.requireNonNull(dispatcher);
        this.parser = Objects.requireNonNull(parser);
        this.includeResolver = Objects.requireNonNull(includeResolver);
        this.artifactProvider = Objects.requireNonNull(artifactProvider);
        this.config = Objects.requireNonNull(config);
    }

    @Override
    public void execute(
            String testName,
            Path testFile,
            ExecutionContext context
    ) {

        Objects.requireNonNull(testName, "testName must not be null");
        Objects.requireNonNull(testFile, "testFile must not be null");
        Objects.requireNonNull(context, "ExecutionContext must not be null");

        try {

            ExecutionContextHolder.set(context);

            // GAP-1: always inject the engine's config before validation
            context.setConfig(config);
            context.validateConfiguration();

            long testStartTime = System.currentTimeMillis();

            publishTestStarted(testName, testFile);

            TestOutcome outcome = TestOutcome.PASSED;
            Exception executionFailure = null;

            List<Step> preparedSteps =
                    parseAndPrepareSteps(testName, testFile);

            StepFlowSplitter.Flow flow =
                    StepFlowSplitter.split(preparedSteps);

            try {
                runMainSteps(testName, flow.main(), context);
            } catch (Exception ex) {
                outcome = TestOutcome.FAILED;
                executionFailure = ex;
            } finally {

                runFinallySteps(testName, flow.fin(), context);

                publishTestFinished(
                        testName,
                        outcome,
                        testStartTime
                );
            }

            if (executionFailure != null) {
                throw new RuntimeException(executionFailure);
            }

        } finally {
            ExecutionContextHolder.clear();
        }
    }

    /* ========================================================== */

    private List<Step> parseAndPrepareSteps(
            String testName,
            Path testFile
    ) {
        List<Step> parsed = parser.parse(testFile);
        return expandIncludes(testName, parsed, testFile.getParent());
    }

    private List<Step> expandIncludes(
            String testName,
            List<Step> steps,
            Path baseDir
    ) {

        List<Step> resolved = new ArrayList<>();

        for (Step step : steps) {

            if ("INCLUDE".equalsIgnoreCase(step.getAction())) {

                resolved.addAll(
                        includeResolver.resolve(
                                testName,
                                step,
                                baseDir
                        )
                );

            } else {
                resolved.add(step);
            }
        }

        return resolved;
    }

    /* ========================================================== */

    private void runMainSteps(
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
        long startTime = System.currentTimeMillis();

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

            Step resolvedStep =
                    StepResolver.resolve(step, context);

            ExecutionResult result;
            try {
                result = dispatcher.dispatch(resolvedStep, context);
            } catch (IllegalStateException varEx) {
                // GAP-2: enrich missing-variable errors with step context
                throw new StepExecutionException(
                        "Step " + stepIndex + " [" + step.getAction() + "]: "
                                + varEx.getMessage()
                );
            }

            context.setLastResult(result);

            if (result.isFailure()) {
                throw new StepExecutionException(
                        "Step " + stepIndex + " [" + step.getAction() + "]: "
                                + result.getMessage()
                );
            }

            long duration =
                    System.currentTimeMillis() - startTime;

            eventPublisher.publish(
                    new StepFinishedEvent(
                            testName,
                            stepIndex,
                            "PASSED",
                            startTime,
                            duration,
                            null
                    )
            );

        } catch (Exception ex) {

            long duration =
                    System.currentTimeMillis() - startTime;

            Optional<Path> artifact = Optional.empty();

            if (artifactProvider != null) {
                try {
                    artifact = artifactProvider.capture(
                            testName,
                            stepIndex,
                            context
                    );
                } catch (Exception ignored) {}
            }

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
                            startTime,
                            duration,
                            artifact.map(Path::toString)
                                    .orElse(ex.getMessage())
                    )
            );

            throw ex;
        }
    }

    /* ========================================================== */

    private void runFinallySteps(
            String testName,
            List<Step> finallySteps,
            ExecutionContext context
    ) {

        if (finallySteps.isEmpty()) {
            return;
        }

        eventPublisher.publish(
                new FinallyStartedEvent(testName)
        );

        for (Step step : finallySteps) {
            try {
                executeSingleStep(testName, step, context);
            } catch (Exception ignored) {
                // finally block never fails test
            }
        }

        eventPublisher.publish(
                new FinallyFinishedEvent(testName)
        );
    }

    /* ========================================================== */

    private void publishTestStarted(
            String testName,
            Path testFile
    ) {
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