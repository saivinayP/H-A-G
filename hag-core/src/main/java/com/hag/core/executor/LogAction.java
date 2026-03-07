package com.hag.core.executor;

import com.hag.core.context.ExecutionContext;
import com.hag.core.dispatcher.descriptor.ActionDescriptor;
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
            ActionDescriptor descriptor,
            ExecutionContext context
    ) {

        Object message =
                context.resolveValue(step.getKey());

        if (message == null) {
            return ExecutionResult.failure(
                    "LOG requires message in key field"
            );
        }

        System.out.println("[HAG] " + message);

        return ExecutionResult.success();
    }
}