package com.hag.core.executor;

import com.hag.core.context.DataScope;
import com.hag.core.context.ExecutionContext;
import com.hag.core.model.Step;
import com.hag.core.result.ExecutionResult;

/**
 * SET Action
 *
 * Stores a resolved value into the GLOBAL scope of DataStore.
 *
 * CSV:
 * SET,variableName,,value
 */
public final class SetContextAction implements Action {

    @Override
    public String name() {
        return "SET";
    }

    @Override
    public ActionCategory category() {
        return ActionCategory.CONTEXT;
    }

    @Override
    public ExecutionResult execute(
            Step step,
            ExecutionContext context
    ) {

        String variableName = step.getRecipient();

        if (variableName == null || variableName.isBlank()) {
            return ExecutionResult.failure(
                    "SET action requires recipient (variable name)"
            );
        }

        Object resolvedValue =
                context.resolveValue(step.getKey());

        context.getDataStore().put(
                DataScope.GLOBAL,
                variableName,
                resolvedValue
        );

        return ExecutionResult.success();
    }
}