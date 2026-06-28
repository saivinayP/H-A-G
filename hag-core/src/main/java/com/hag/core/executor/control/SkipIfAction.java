package com.hag.core.executor.control;

import com.hag.core.context.ExecutionContext;
import com.hag.core.dispatcher.descriptor.ActionDescriptor;
import com.hag.core.executor.Action;
import com.hag.core.executor.ConditionEvaluator;
import com.hag.core.model.Step;
import com.hag.core.result.ExecutionResult;

/**
 * Skips the NEXT step if the condition aligns with the subcase.
 * Syntax:
 * Action: SKIP:IF or SKIP:UNLESS
 * Target: condition
 */
public class SkipIfAction implements Action {

    @Override
    public String name() {
        return "SKIP";
    }

    @Override
    public com.hag.core.executor.ActionCategory category() {
        return com.hag.core.executor.ActionCategory.CORE;
    }

    @Override
    public ExecutionResult execute(Step step, ActionDescriptor descriptor, ExecutionContext context) {
        if (!"IF".equalsIgnoreCase(descriptor.subCase()) && !"UNLESS".equalsIgnoreCase(descriptor.subCase())) {
            return ExecutionResult.skipped();
        }
        String condition = step.getRecipient();

        if (condition == null || condition.isBlank()) {
            return ExecutionResult.failure("SKIP action requires a condition in the Target column.");
        }

        boolean isTrue = ConditionEvaluator.evaluate(condition, context);
        boolean shouldSkip = "IF".equalsIgnoreCase(descriptor.subCase()) ? isTrue : !isTrue;

        if (shouldSkip) {
            context.setSkipNextStep(true);
        }

        return ExecutionResult.success();
    }
}
