package com.hag.core.engine.executor.validation;

import com.hag.core.engine.context.ExecutionContext;
import com.hag.core.engine.executor.StepExecutor;
import com.hag.core.engine.model.Step;
import com.hag.core.engine.result.EmptyExecutionResult;
import com.hag.core.engine.result.ExecutionResult;

public class CompareValuesExecutor implements StepExecutor {

    @Override
    public ExecutionResult execute(Step step, ExecutionContext context) {
        String left = step.getRecipient();
        String operator = step.getSource();
        String right = step.getKey();

        Object leftVal = context.resolveValue(left);
        Object rightVal = context.resolveValue(right);

        ComparisonOperator op = ComparisonOperator.valueOf(operator.toUpperCase());
        boolean result = switch (op) {
            case EQUALS -> leftVal.equals(rightVal);
            case NOT_EQUALS -> !leftVal.equals(rightVal);
            case CONTAINS -> leftVal.toString().contains(rightVal.toString());
            case GREATER_THAN -> Double.parseDouble(leftVal.toString()) > Double.parseDouble(rightVal.toString());
            case LESS_THAN -> Double.parseDouble(leftVal.toString()) < Double.parseDouble(rightVal.toString());
        };

        if (!result) {
            throw new AssertionError("Comparison failed: " + leftVal + " " + rightVal);
        }

        return EmptyExecutionResult.INSTANCE;
    }
}
