package com.hag.core.executor;

import com.hag.core.context.ExecutionContext;
import com.hag.core.executor.Action;
import com.hag.core.executor.ActionCategory;
import com.hag.core.model.Step;
import com.hag.core.result.ExecutionResult;

public final class LogAction implements Action {

    @Override
    public String name() {
        return "LOG";
    }

    @Override
    public ActionCategory category() {
        return ActionCategory.CORE;
    }

    @Override
    public ExecutionResult execute(
            Step step,
            ExecutionContext context
    ) {

        String message = step.getKey();

        if (message == null || message.isBlank()) {
            return ExecutionResult.failure(
                    "LOG action requires message in key field"
            );
        }

        System.out.println("[HAG-LOG] " + message);

        return ExecutionResult.success();
    }
}