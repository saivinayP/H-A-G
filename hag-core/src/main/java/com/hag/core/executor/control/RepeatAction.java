package com.hag.core.executor.control;

import com.hag.core.context.ExecutionContext;
import com.hag.core.dispatcher.descriptor.ActionDescriptor;
import com.hag.core.executor.Action;
import com.hag.core.model.Step;
import com.hag.core.result.ExecutionResult;

/**
 * Repeats a subscript N times.
 * Action: REPEAT:N
 * Target: 5
 * Value: subscript.csv
 */
public class RepeatAction implements Action {

    @Override
    public String name() {
        return "REPEAT";
    }

    @Override
    public com.hag.core.executor.ActionCategory category() {
        return com.hag.core.executor.ActionCategory.CORE;
    }

    @Override
    public ExecutionResult execute(Step step, ActionDescriptor descriptor, ExecutionContext context) {
        if (!"N".equalsIgnoreCase(descriptor.subCase())) {
            return ExecutionResult.skipped();
        }
        String timesStr = step.getRecipient();
        String subscript = step.getSource();

        if (timesStr == null || timesStr.isBlank()) {
            return ExecutionResult.failure("REPEAT:N requires a number in the Target column.");
        }
        if (subscript == null || subscript.isBlank()) {
            return ExecutionResult.failure("REPEAT:N requires a script path in the Value column.");
        }

        int times;
        try {
            times = Integer.parseInt(timesStr.trim());
        } catch (NumberFormatException e) {
            return ExecutionResult.failure("REPEAT:N target must be a valid integer, got: " + timesStr);
        }

        if (context.getEngine() == null) {
            return ExecutionResult.failure("ExecutionEngine is not available in the context.");
        }

        for (int i = 0; i < times; i++) {
            context.getDataStore().put("LOOP_INDEX", i);
            context.getDataStore().put("LOOP_1_INDEX", i + 1);

            ExecutionResult result = context.getEngine().runSubscript("Repeat-" + subscript + "-" + i, subscript, context);
            if (result.isFailure()) {
                return result; // Fast fail
            }
        }

        return ExecutionResult.success();
    }
}
