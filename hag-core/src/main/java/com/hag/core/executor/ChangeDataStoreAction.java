package com.hag.core.executor;

import com.hag.core.context.DataScope;
import com.hag.core.context.ExecutionContext;
import com.hag.core.dispatcher.descriptor.ActionDescriptor;
import com.hag.core.dispatcher.descriptor.ModifierSet;
import com.hag.core.model.Step;
import com.hag.core.result.ExecutionResult;

/**
 * CHANGE_DATA_STORE action — stores or updates a variable in the DataStore.
 *
 * <h3>CSV Syntax</h3>
 * <pre>
 *   CHANGE_DATA_STORE,variableName,,literalValue
 *   CHANGE_DATA_STORE,variableName,,${EXISTING_VAR}
 *   CHANGE_DATA_STORE,variableName,scope=GLOBAL,value
 *   CHANGE_DATA_STORE:DELETE,variableName,,
 * </pre>
 *
 * <ul>
 *   <li><b>Recipient</b> — variable name to create / update</li>
 *   <li><b>Source</b>    — optional modifier: {@code scope=UI|API|DB|GLOBAL}</li>
 *   <li><b>Key</b>       — value to store (supports {@code ${VAR}} interpolation)</li>
 * </ul>
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

        // Resolve target scope from Source modifier "scope=<SCOPE>"
        ModifierSet modifiers = step.getModifiers();
        DataScope scope = resolveScope(modifiers);

        // :DELETE sub-case — remove the variable
        if (descriptor.isSubCase("DELETE")) {
            context.getDataStore().remove(scope, variableName);
            return ExecutionResult.success();
        }

        // Default — store the value
        Object value = context.resolveValue(step.getKey());
        context.getDataStore().put(scope, variableName, value);

        return ExecutionResult.success();
    }

    private DataScope resolveScope(ModifierSet modifiers) {

        if (modifiers == null) return DataScope.GLOBAL;

        String scopeParam = modifiers.getParameter("scope");
        if (scopeParam == null) return DataScope.GLOBAL;

        try {
            return DataScope.valueOf(scopeParam.toUpperCase());
        } catch (IllegalArgumentException e) {
            return DataScope.GLOBAL;
        }
    }
}
