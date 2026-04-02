package com.hag.core.executor.control;

import com.hag.core.context.ExecutionContext;
import com.hag.core.dispatcher.descriptor.ActionDescriptor;
import com.hag.core.executor.Action;
import com.hag.core.executor.ConditionEvaluator;
import com.hag.core.model.Step;
import com.hag.core.result.ExecutionResult;

/**
 * Executes a subscript if the condition is true.
 * Syntax: EXECUTE:IF | condition | true_script [ | false_script ]
 */
public class ExecuteIfAction implements Action {

    @Override
    public String name() {
        return "EXECUTE";
    }

    @Override
    public com.hag.core.executor.ActionCategory category() {
        return com.hag.core.executor.ActionCategory.CORE;
    }

    @Override
    public ExecutionResult execute(Step step, ActionDescriptor descriptor, ExecutionContext context) {
        if (!"IF".equalsIgnoreCase(descriptor.subCase())) {
            return ExecutionResult.skipped();
        }
        String condition = step.getRecipient(); // Target column (var=value)
        String scriptParams = step.getSource(); // Value column

        if (condition == null || condition.isBlank()) {
            return ExecutionResult.failure("EXECUTE:IF requires a condition in the Target column.");
        }
        if (scriptParams == null || scriptParams.isBlank()) {
            return ExecutionResult.failure("EXECUTE:IF requires a script path in the Value column.");
        }

        String trueScript = scriptParams;
        String falseScript = null;

        if (scriptParams.contains("|")) {
            String[] parts = scriptParams.split("\\|", 2);
            trueScript = parts[0].trim();
            falseScript = parts[1].trim();
        }

        boolean isTrue = ConditionEvaluator.evaluate(condition, context);
        String scriptToRun = isTrue ? trueScript : falseScript;

        if (scriptToRun != null && !scriptToRun.isBlank()) {
            String testName = "Subscript-" + scriptToRun;
            if (context.getEngine() != null) {
                return context.getEngine().runSubscript(testName, scriptToRun, context);
            } else {
                return ExecutionResult.failure("ExecutionEngine is not available in the context.");
            }
        }

        // Neither script was specified or condition was false and no false script given. No op.
        return ExecutionResult.success();
    }
}
