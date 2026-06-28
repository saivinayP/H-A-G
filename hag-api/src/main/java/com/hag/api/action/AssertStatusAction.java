package com.hag.api.action;

import com.hag.api.model.ApiResponse;
import com.hag.core.context.ExecutionContext;
import com.hag.core.dispatcher.descriptor.ActionDescriptor;
import com.hag.core.executor.Action;
import com.hag.core.executor.ActionCategory;
import com.hag.core.model.Step;
import com.hag.core.result.ExecutionResult;

/**
 * ASSERT_STATUS action — asserts the HTTP status code of the last API response.
 *
 * <h3>CSV Syntax</h3>
 * <pre>
 *   ASSERT_STATUS,,,200
 *   ASSERT_STATUS,,,201
 *   ASSERT_STATUS:NOT,,,404
 * </pre>
 *
 * <ul>
 *   <li>Plain {@code ASSERT_STATUS} — exact match</li>
 *   <li>{@code ASSERT_STATUS:NOT} — assert status is NOT the given code</li>
 *   <li>{@code ASSERT_STATUS:2XX} — assert status is in 200-299 range</li>
 *   <li>{@code ASSERT_STATUS:4XX} — assert status is in 400-499 range</li>
 * </ul>
 */
public final class AssertStatusAction implements Action {

    @Override
    public String name() { return "ASSERT_STATUS"; }

    @Override
    public ActionCategory category() { return ActionCategory.API; }

    @Override
    public ExecutionResult execute(Step step, ActionDescriptor descriptor, ExecutionContext context) {

        ApiResponse response = getLastResponse(context);
        if (response == null) {
            return ExecutionResult.failure("ASSERT_STATUS — no API response found. Did SEND_REQUEST run first?");
        }

        String expected = step.getKey();
        int actual = response.statusCode();

        // Range sub-cases
        if (descriptor.isSubCase("2XX")) {
            return actual >= 200 && actual < 300
                    ? ExecutionResult.success()
                    : ExecutionResult.failure("ASSERT_STATUS:2XX failed — actual status: " + actual);
        }
        if (descriptor.isSubCase("4XX")) {
            return actual >= 400 && actual < 500
                    ? ExecutionResult.success()
                    : ExecutionResult.failure("ASSERT_STATUS:4XX failed — actual status: " + actual);
        }
        if (descriptor.isSubCase("5XX")) {
            return actual >= 500 && actual < 600
                    ? ExecutionResult.success()
                    : ExecutionResult.failure("ASSERT_STATUS:5XX failed — actual status: " + actual);
        }

        if (expected == null || expected.isBlank()) {
            return ExecutionResult.failure("ASSERT_STATUS requires expected code in Key column");
        }

        int expectedCode;
        try {
            expectedCode = Integer.parseInt(expected.trim());
        } catch (NumberFormatException e) {
            return ExecutionResult.failure("ASSERT_STATUS — expected value is not a valid HTTP status code: " + expected);
        }

        if (descriptor.isSubCase("NOT")) {
            return actual != expectedCode
                    ? ExecutionResult.success()
                    : ExecutionResult.failure("ASSERT_STATUS:NOT failed — status was " + actual);
        }

        // Default — exact match
        return actual == expectedCode
                ? ExecutionResult.success()
                : ExecutionResult.failure(
                        "ASSERT_STATUS failed — expected: " + expectedCode + " actual: " + actual
                );
    }

    private ApiResponse getLastResponse(ExecutionContext context) {
        return context.getDataStore()
                .get(SendRequestAction.LAST_RESPONSE_KEY)
                .filter(v -> v instanceof ApiResponse)
                .map(v -> (ApiResponse) v)
                .orElse(null);
    }
}
