package com.hag.core.engine.executor.ui;

import com.hag.core.engine.context.ExecutionContext;
import com.hag.core.engine.executor.StepExecutor;
import com.hag.core.engine.model.Step;
import com.hag.core.engine.resolver.LocatorReference;
import com.hag.core.engine.result.EmptyExecutionResult;
import com.hag.core.engine.result.ExecutionResult;

public class TypeExecutor implements StepExecutor {

    @Override
    public ExecutionResult execute(
            Step step,
            ExecutionContext context
    ) {

        LocatorReference ref =
                LocatorReference.parse(step.getRecipient());

        Object value =
                context.getTestDataResolver()
                        .resolve(step.getSource(), step.getKey());

        context.getUiAdapter()
                .type(
                        ref.getLocatorFile(),
                        ref.getElementName(),
                        value == null ? "" : value.toString()
                );

        return EmptyExecutionResult.INSTANCE;
    }
}
