package com.hag.core.engine.executor.validation;

import com.hag.core.engine.context.ExecutionContext;
import com.hag.core.engine.executor.StepExecutor;
import com.hag.core.engine.model.Step;
import com.hag.core.engine.result.ExecutionResult;
import com.hag.core.engine.result.EmptyExecutionResult;

public class TextMatchesExecutor implements StepExecutor {

    @Override
    public ExecutionResult execute(Step step, ExecutionContext context) {
        Object actual = context.resolveValue(step.getRecipient());
        String mode = step.getSource();
        String expected = step.getKey();

        TextMatchMode matchMode = TextMatchMode.valueOf(mode.toUpperCase());
        boolean match = switch (matchMode) {
            case EXACT -> actual.toString().equals(expected);
            case IGNORE_CASE -> actual.toString().equalsIgnoreCase(expected);
            case CONTAINS -> actual.toString().contains(expected);
        };

        if (!match) {
            throw new AssertionError("Text match failed [" + matchMode + "]: " + "actual='" + actual + "', expected='" + expected + "'");
        }

        return EmptyExecutionResult.INSTANCE;
    }
}
