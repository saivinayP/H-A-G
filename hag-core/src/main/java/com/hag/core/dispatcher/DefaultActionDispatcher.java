package com.hag.core.dispatcher;

import com.hag.core.context.ExecutionContext;
import com.hag.core.dispatcher.descriptor.ActionDescriptor;
import com.hag.core.dispatcher.descriptor.ActionDescriptorParser;
import com.hag.core.dispatcher.descriptor.ModifierSet;
import com.hag.core.dispatcher.retry.RetryExecutor;
import com.hag.core.executor.Action;
import com.hag.core.model.Step;
import com.hag.core.result.ExecutionResult;

/**
 * Default dispatcher — parses the Action column, resolves Source modifiers,
 * looks up the registered action by name, and delegates execution.
 *
 * <p>Dispatch sequence:
 * <ol>
 *   <li>Parse {@code ACTION:SUBCASE} from the step's Action column</li>
 *   <li>Parse Source column into a {@link ModifierSet} and attach to the step</li>
 *   <li>Resolve the action from the {@link ActionRegistry} by primary name</li>
 *   <li>Execute with optional retry</li>
 * </ol>
 */
public final class DefaultActionDispatcher implements ActionDispatcher {

    private final ActionRegistry registry;

    public DefaultActionDispatcher(ActionRegistry registry) {
        this.registry = registry;
    }

    @Override
    public ExecutionResult dispatch(Step step, ExecutionContext context) {

        // Parse ACTION:SUBCASE from the Action column (static call)
        ActionDescriptor descriptor =
                ActionDescriptorParser.parse(step.getAction());

        // Parse Source column into ModifierSet and attach to the step
        ModifierSet modifiers =
                ActionDescriptorParser.parseModifiers(step.getSource());
        step.setModifiers(modifiers);

        // Resolve all actions matching the primary name
        java.util.List<Action> actions = registry.resolveAll(descriptor.name());
        if (actions.isEmpty()) {
            throw new IllegalStateException(
                    "No action registered for: [" + descriptor.name()
                            + "]. Full action string: [" + step.getAction() + "]"
            );
        }

        // Sub-case validation is delegated to the action implementation
        int retryCount = resolveRetryCount(modifiers);

        return RetryExecutor.executeWithRetry(
                retryCount,
                () -> {
                    for (Action action : actions) {
                        ExecutionResult result = action.execute(step, descriptor, context);
                        if (result.getStatus() != ExecutionResult.Status.SKIPPED) {
                            return result;
                        }
                    }
                    return ExecutionResult.failure(
                        "No specific handler found for [" + step.getAction() + "] among " + actions.size() + " registrations"
                    );
                }
        );
    }

    /**
     * Reads the {@code retry=N} parameter from the Source-column modifiers.
     * Defaults to 1 (no retry) if absent or unparseable.
     */
    private int resolveRetryCount(ModifierSet modifiers) {
        String retryParam = modifiers.getParameter("retry");
        if (retryParam == null) return 1;
        try {
            return Math.max(Integer.parseInt(retryParam), 1);
        } catch (NumberFormatException ex) {
            return 1;
        }
    }
}