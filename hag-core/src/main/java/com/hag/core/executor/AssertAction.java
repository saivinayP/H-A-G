package com.hag.core.executor;

import com.hag.core.context.ExecutionContext;
import com.hag.core.model.Step;
import com.hag.core.result.ExecutionResult;

import java.util.Objects;

/**
 * ASSERT Action
 *
 * CSV Format:
 * ASSERT,actual,,expected
 * ASSERT,actual,CONTAINS,expected
 *
 * Default operator: EQUALS
 */
public final class AssertAction implements Action {

    private static final String DEFAULT_OPERATOR = "EQUALS";

    @Override
    public String name() {
        return "ASSERT";
    }

    @Override
    public ActionCategory category() {
        return ActionCategory.CORE;
    }

    @Override
    public ExecutionResult execute(
            Step step,
            ExecutionContext context
    ) {

        Object actual =
                context.resolveValue(step.getRecipient());

        Object expected =
                context.resolveValue(step.getKey());

        String operator =
                step.getSource() == null || step.getSource().isBlank()
                        ? DEFAULT_OPERATOR
                        : step.getSource().trim().toUpperCase();

        boolean result = evaluate(operator, actual, expected);

        if (!result) {
            return ExecutionResult.failure(
                    buildFailureMessage(operator, actual, expected)
            );
        }

        return ExecutionResult.success();
    }

    private boolean evaluate(
            String operator,
            Object actual,
            Object expected
    ) {

        return switch (operator) {
            case "EQUALS" ->
                    Objects.equals(actual, expected);

            case "NOT_EQUALS" ->
                    !Objects.equals(actual, expected);

            case "CONTAINS" ->
                    actual != null
                            && expected != null
                            && actual.toString()
                            .contains(expected.toString());

            default ->
                    throw new IllegalArgumentException(
                            "Unsupported ASSERT operator: " + operator
                    );
        };
    }

    private String buildFailureMessage(
            String operator,
            Object actual,
            Object expected
    ) {

        return "ASSERT failed: ["
                + operator
                + "] actual=<"
                + actual
                + "> expected=<"
                + expected
                + ">";
    }
}