package com.hag.core.engine.executor.ui;

import com.hag.core.engine.context.ExecutionContext;
import com.hag.core.engine.executor.StepExecutor;
import com.hag.core.engine.model.Step;
import com.hag.core.engine.resolver.LocatorReference;
import com.hag.core.engine.result.EmptyExecutionResult;
import com.hag.core.engine.result.ExecutionResult;

public class ClickExecutor implements StepExecutor {

    @Override
    public ExecutionResult execute(Step step, ExecutionContext context) {

        LocatorReference ref = LocatorReference.parse(step.getRecipient());
        String action = step.getAction();

        if (action.contains("[DOUBLE]")) {
            context.getUiAdapter().doubleClick(ref.getLocatorFile(), ref.getElementName());
        } else if (action.contains("[RIGHT]")) {
            context.getUiAdapter().rightClick(ref.getLocatorFile(), ref.getElementName());
        } else {
            context.getUiAdapter().click(ref.getLocatorFile(), ref.getElementName());
        }

        return EmptyExecutionResult.INSTANCE;
    }
}
