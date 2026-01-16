package com.hag.core.engine.executor.validation.ui;

import com.hag.core.engine.context.ExecutionContext;
import com.hag.core.engine.executor.StepExecutor;
import com.hag.core.engine.model.Step;
import com.hag.core.engine.resolver.LocatorReference;
import com.hag.core.engine.result.EmptyExecutionResult;
import com.hag.core.engine.result.ExecutionResult;

public class IsDisplayedExecutor implements StepExecutor {

    @Override
    public ExecutionResult execute(Step step, ExecutionContext context) {
        boolean negate = step.getAction().contains("[NOT]");
        LocatorReference ref = LocatorReference.parse(step.getRecipient());
        boolean actual = context.getUiAdapter()
                .isDisplayed(
                        ref.getLocatorFile(),
                        ref.getElementName()
                );

        if (!negate && !actual) {
            throw new AssertionError(
                    "Expected element to be displayed: " +
                            step.getRecipient()
            );
        }

        if (negate && actual) {
            throw new AssertionError(
                    "Expected element NOT to be displayed: " +
                            step.getRecipient()
            );
        }

        return EmptyExecutionResult.INSTANCE;
    }
}
