package com.hag.core.engine.executor.ui;

import com.hag.core.engine.context.ExecutionContext;
import com.hag.core.engine.executor.StepExecutor;
import com.hag.core.engine.model.Step;
import com.hag.core.engine.resolver.LocatorReference;
import com.hag.core.engine.result.EmptyExecutionResult;
import com.hag.core.engine.result.ExecutionResult;

public class SwitchToExecutor implements StepExecutor {
    @Override
    public ExecutionResult execute(Step step, ExecutionContext context) {
        String subAction = step.getAction().toUpperCase();

        if (subAction.contains("[FRAME]")) {
            if ("DEFAULT".equalsIgnoreCase(step.getRecipient())) {
                context.getUiAdapter().switchToDefaultContent();
            } else {
                LocatorReference ref = LocatorReference.parse(step.getRecipient());
                context.getUiAdapter().switchToFrame(ref.getLocatorFile(), ref.getElementName());
            }
        } else if (subAction.contains("[WINDOW]")) {
            context.getUiAdapter().switchToWindow(step.getRecipient());
        }

        return EmptyExecutionResult.INSTANCE;
    }
}