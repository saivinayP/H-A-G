package com.hag.core.engine.executor.ui;

import com.hag.core.engine.context.ExecutionContext;
import com.hag.core.engine.executor.StepExecutor;
import com.hag.core.engine.model.Step;
import com.hag.core.engine.resolver.LocatorReference;
import com.hag.core.engine.result.EmptyExecutionResult;
import com.hag.core.engine.result.ExecutionResult;

public class WaitExecutor implements StepExecutor {
    @Override
    public ExecutionResult execute(Step step, ExecutionContext context) {
        String condition = step.getAction().toUpperCase().contains("[VISIBLE]") ? "VISIBLE" : "HIDDEN";
        int timeout = Integer.parseInt(step.getSource() != null ? step.getSource() : "10");
        LocatorReference ref = LocatorReference.parse(step.getRecipient());

        context.getUiAdapter().waitFor(ref.getLocatorFile(), ref.getElementName(), condition, timeout);
        return EmptyExecutionResult.INSTANCE;
    }
}