package com.hag.core.engine;

import com.hag.core.engine.context.ExecutionContext;
import com.hag.core.engine.context.ResolvedStep;
import com.hag.core.engine.dispatcher.ActionDispatcher;
import com.hag.core.engine.dispatcher.ControlActions;
import com.hag.core.engine.model.Step;
import com.hag.core.engine.parser.CsvTestParser;
import com.hag.core.engine.parser.IncludeResolver;
import com.hag.core.engine.result.ExecutionResult;
import com.hag.core.reporting.engine.EventPublisher;
import com.hag.core.reporting.events.*;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * DefaultExecutionEngine
 *
 * Core orchestrator of test execution in H.A.G framework.
 *
 * Responsibilities:
 *  - Validate runtime configuration
 *  - Parse CSV test file
 *  - Expand include directives
 *  - Split normal and finally flows
 *  - Execute steps sequentially
 *  - Guarantee finally execution
 *  - Publish lifecycle events
 *
 * This class DOES NOT:
 *  - Contain UI/API/DB logic
 *  - Interpret business rules
 *  - Resolve locators
 *  - Perform HTTP or DB calls
 *
 * It only orchestrates execution.
 */
public class DefaultExecutionEngine implements ExecutionEngine {

    private final EventPublisher eventPublisher;
    private final ActionDispatcher dispatcher;
    private final CsvTestParser parser;
    private final IncludeResolver includeResolver;

    /**
     * Constructor injection enforces immutability and
     * ensures engine dependencies are always present.
     */
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

    /**
     * Entry point for executing a test.
     *
     * Execution lifecycle:
     *  1. Validate configuration
     *  2. Publish TestStarted
     *  3. Parse + expand includes
     *  4. Split main and finally flows
     *  5. Execute main steps
     *  6. Always execute finally steps
     *  7. Publish TestFinished
     *
     * If any main step fails:
     *  - Test outcome becomes FAILED
     *  - Exception is rethrown after finally execution
     */
    @Override
    public void execute(
            String testName,
            Path testFile,
            ExecutionContext context
    ) {

        Objects.requireNonNull(context, "ExecutionContext must not be null");

        // Ensure adapters & resolvers are configured before execution begins
        context.validateConfiguration();

        long testStartTime = System.currentTimeMillis();
        publishTestStarted(testName, testFile);

        TestOutcome outcome = TestOutcome.PASSED;
        Exception executionFailure = null;

        // Parse CSV and resolve include directives
        List<Step> allSteps =
                parseAndPrepareSteps(testName, testFile);

        // Separate main flow and finally flow
        StepFlowSplitter.Flow flow =
                StepFlowSplitter.split(allSteps);

        try {
            runSteps(testName, flow.main(), context);
        } catch (Exception ex) {
            outcome = TestOutcome.FAILED;
            executionFailure = ex;
        } finally {

            // Finally steps must always execute regardless of failure
            runFinallySteps(testName, flow.fin(), context);

            publishTestFinished(testName, outcome, testStartTime);
        }

        // Preserve original failure after lifecycle completion
        if (executionFailure != null) {
            throw new RuntimeException(executionFailure);
        }
    }

    /* ==========================================================
       Parsing & Include Resolution
       ========================================================== */

    /**
     * Parses the CSV file and expands include directives.
     */
    private List<Step> parseAndPrepareSteps(
            String testName,
            Path testFile
    ) {

        List<Step> parsedSteps = parser.parse(testFile);

        return expandIncludes(
                testName,
                parsedSteps,
                testFile.getParent()
        );
    }

    /**
     * Replaces INCLUDE steps with actual resolved steps.
     * Prevents nested include logic from leaking into execution layer.
     */
    private List<Step> expandIncludes(
            String testName,
            List<Step> steps,
            Path baseDir
    ) {

        List<Step> resolved = new ArrayList<>();

        for (Step step : steps) {

            if (ControlActions.INCLUDE.equalsIgnoreCase(step.getAction())) {

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

    /* ==========================================================
       Step Execution
       ========================================================== */

    /**
     * Executes main steps sequentially.
     * Stops execution on first failure.
     */
    private void runSteps(
            String testName,
            List<Step> steps,
            ExecutionContext context
    ) throws Exception {

        for (Step step : steps) {
            executeSingleStep(testName, step, context);
        }
    }

    /**
     * Executes a single step and publishes step-level events.
     *
     * Execution contract:
     *  - StepStartedEvent always emitted
     *  - StepFinishedEvent always emitted
     *  - StepFailedEvent emitted only on failure
     *  - ExecutionResult stored in context
     */
    private void executeSingleStep(
            String testName,
            Step step,
            ExecutionContext context
    ) throws Exception {

        int stepIndex = context.nextStepIndex();
        long stepStartTime = System.currentTimeMillis();

        // Notify reporting layer that step has started
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

            // Resolve placeholders and dynamic values
            Step resolvedStep =
                    ResolvedStep.resolve(step, context);

            // Dispatch to correct executor
            ExecutionResult result =
                    dispatcher.dispatch(resolvedStep, context);

            // Persist result for future steps (StoreData, validation, etc.)
            context.setLastResult(result);

            long duration =
                    System.currentTimeMillis() - stepStartTime;

            eventPublisher.publish(
                    new StepFinishedEvent(
                            testName,
                            stepIndex,
                            "PASSED",
                            stepStartTime,
                            duration,
                            null
                    )
            );

        } catch (Exception ex) {

            long duration =
                    System.currentTimeMillis() - stepStartTime;

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
                            duration,
                            ex.getMessage()
                    )
            );

            throw ex;
        }
    }

    /* ==========================================================
       Finally Handling
       ========================================================== */

    /**
     * Executes finally steps.
     * Failures inside finally block DO NOT fail the test.
     */
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
                // intentionally suppressed
            }
        }

        eventPublisher.publish(
                new FinallyFinishedEvent(testName)
        );
    }

    /* ==========================================================
       Test Lifecycle Events
       ========================================================== */

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

    /**
     * Internal outcome tracking.
     */
    private enum TestOutcome {
        PASSED,
        FAILED
    }
}
