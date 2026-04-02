package com.hag.core.executor;

import com.hag.core.context.ExecutionContext;
import com.hag.core.dispatcher.descriptor.ActionDescriptor;
import com.hag.core.model.Step;
import com.hag.core.result.ExecutionResult;

/**
 * CHANGE_DATA_STORE action — creates, updates, or deletes variables in the flat global DataStore.
 *
 * <h3>Subcases</h3>
 * <ul>
 *   <li>{@code :SET} — Create or overwrite (always succeeds)</li>
 *   <li>{@code :UPDATE} — Update only if already exists; fails if not set</li>
 *   <li>{@code :DELETE} — Remove the variable</li>
 * </ul>
 *
 * <h3>CSV Syntax</h3>
 * <pre>
 *   CHANGE_DATA_STORE:SET,variableName,,literalValue
 *   CHANGE_DATA_STORE:UPDATE,variableName,,${NEW_VALUE}
 *   CHANGE_DATA_STORE:DELETE,variableName,,
 * </pre>
 */
public final class ChangeDataStoreAction implements Action {

    @Override
    public String name() {
        return "CHANGE_DATA_STORE";
    }

    @Override
    public ActionCategory category() {
        return ActionCategory.CONTEXT;
    }

    @Override
    public ExecutionResult execute(
            Step step,
            ActionDescriptor descriptor,
            ExecutionContext context
    ) {
        String variableName = step.getRecipient();

        if (variableName == null || variableName.isBlank()) {
            return ExecutionResult.failure(
                    "CHANGE_DATA_STORE requires Recipient as the variable name"
            );
        }

        // :DELETE sub-case — remove the variable
        if (descriptor.isSubCase("DELETE")) {
            context.getDataStore().remove(variableName);
            return ExecutionResult.success();
        }

        // :UPDATE sub-case — fail if old variable doesn't exist
        if (descriptor.isSubCase("UPDATE")) {
            if (!context.getDataStore().containsKey(variableName)) {
                return ExecutionResult.failure(
                        "Cannot UPDATE variable '" + variableName + "' because it does not exist in the DataStore"
                );
            }
        }

        // :SET (or default if omitted) — store the value
        Object value = context.resolveValue(step.getKey());
        context.getDataStore().put(variableName, value);

        return ExecutionResult.success();
    }
}
