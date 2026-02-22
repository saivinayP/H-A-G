package com.hag.core.engine.dispatcher;

import com.hag.core.engine.context.ExecutionContext;
import com.hag.core.engine.executor.StepExecutor;
import com.hag.core.engine.model.Step;
import com.hag.core.engine.result.ExecutionResult;

import java.util.Map;
import java.util.Objects;

/**
 * Responsible for routing a resolved Step
 * to the correct executor implementation.
 *
 * This class is intentionally:
 *  - Stateless
 *  - Pure
 *  - Non-logging
 *  - Non-reporting
 *
 * It must NOT:
 *  - Publish events
 *  - Modify ExecutionContext
 *  - Swallow exceptions
 */
public class ActionDispatcher {

    private final Map<String, StepExecutor> registry;

    public ActionDispatcher(Map<String, StepExecutor> registry) {
        this.registry = Objects.requireNonNull(registry);
    }

    /**
     * Dispatches a step to the appropriate executor.
     *
     * @throws IllegalArgumentException if action is unknown
     */
    public ExecutionResult dispatch(
            Step step,
            ExecutionContext context
    ) throws Exception
    {

        Objects.requireNonNull(step, "Step cannot be null");
        Objects.requireNonNull(context, "ExecutionContext cannot be null");

        String rawAction = step.getAction();

        ActionParts parts = parseAction(rawAction);

        StepExecutor executor =
                registry.get(parts.baseAction());

        if (executor == null) {
            throw new IllegalArgumentException(
                    "Unknown action: " + rawAction
            );
        }

        return executor.execute(step, context);
    }

    /**
     * Parses action string into base action and optional case.
     *
     * Example:
     *  Click[DOUBLE] → base=Click, case=DOUBLE
     *  Type → base=Type, case=null
     */
    private ActionParts parseAction(String rawAction) {

        if (rawAction == null || rawAction.isBlank()) {
            throw new IllegalArgumentException(
                    "Action cannot be null or blank"
            );
        }

        int bracketStart = rawAction.indexOf('[');

        if (bracketStart == -1) {
            return new ActionParts(rawAction.trim(), null);
        }

        int bracketEnd = rawAction.indexOf(']');

        if (bracketEnd == -1 || bracketEnd < bracketStart) {
            throw new IllegalArgumentException(
                    "Malformed action case syntax: " + rawAction
            );
        }

        String base =
                rawAction.substring(0, bracketStart).trim();

        String caseValue =
                rawAction.substring(bracketStart + 1, bracketEnd).trim();

        if (caseValue.isBlank()) {
            throw new IllegalArgumentException(
                    "Empty case modifier in action: " + rawAction
            );
        }

        return new ActionParts(base, caseValue);
    }

    /**
     * Internal value object for parsed action.
     */
    private record ActionParts(
            String baseAction,
            String caseValue
    ) {}
}