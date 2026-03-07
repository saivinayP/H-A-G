package com.hag.core.executor;

import com.hag.core.context.ExecutionContext;
import com.hag.core.dispatcher.descriptor.ActionDescriptor;
import com.hag.core.model.Step;
import com.hag.core.result.ExecutionResult;

import java.util.Objects;

public final class AssertAction implements Action {

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
            ActionDescriptor descriptor,
            ExecutionContext context
    ) {

        Object actual =
                context.resolveValue(step.getRecipient());

        Object expected =
                context.resolveValue(step.getKey());

        String operator =
                descriptor.getParameter("op") != null
                        ? descriptor.getParameter("op").toUpperCase()
                        : "EQUALS";

        boolean result = evaluate(operator, actual, expected);

        if (!result) {
            return ExecutionResult.failure(
                    "ASSERT failed [" + operator + "] actual=<"
                            + actual + "> expected=<" + expected + ">"
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
                            "Unsupported ASSERT operator: "
                                    + operator
                    );
        };
    }
}