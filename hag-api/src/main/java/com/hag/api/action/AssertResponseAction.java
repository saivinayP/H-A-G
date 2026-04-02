package com.hag.api.action;

import com.hag.api.model.ApiResponse;
import com.hag.api.util.JsonPathExtractor;
import com.hag.core.context.ExecutionContext;
import com.hag.core.dispatcher.descriptor.ActionDescriptor;
import com.hag.core.executor.Action;
import com.hag.core.executor.ActionCategory;
import com.hag.core.model.Step;
import com.hag.core.result.ExecutionResult;

/**
 * ASSERT_RESPONSE action — asserts a value within the last API response body.
 *
 * <h3>CSV Syntax</h3>
 * <pre>
 *   ASSERT_RESPONSE,data.token,,abc123              → exact match
 *   ASSERT_RESPONSE:CONTAINS,data.message,,success  → contains check
 *   ASSERT_RESPONSE:NOT_NULL,data.token,,           → field exists and non-null
 *   ASSERT_RESPONSE:NULL,data.errorCode,,           → field is null or absent
 *   ASSERT_RESPONSE:EQUALS,data.status,,ACTIVE      → explicit equals
 *   ASSERT_RESPONSE:NOT_EQUALS,data.status,,DELETED → not equals
 *   ASSERT_RESPONSE:HEADER,Content-Type,,application/json  → assert header value
 * </pre>
 *
 * <ul>
 *   <li><b>Recipient</b> — dot-notation JSON path, e.g. {@code data.user.id}</li>
 *   <li><b>Key</b>       — expected value</li>
 * </ul>
 */
public final class AssertResponseAction implements Action {

    @Override
    public String name() { return "ASSERT_RESPONSE"; }

    @Override
    public ActionCategory category() { return ActionCategory.API; }

    @Override
    public ExecutionResult execute(Step step, ActionDescriptor descriptor, ExecutionContext context) {

        ApiResponse response = getLastResponse(context);
        if (response == null) {
            return ExecutionResult.failure("ASSERT_RESPONSE — no API response found. Run SEND_REQUEST first.");
        }

        String path     = step.getRecipient();
        String expected = resolveExpected(step, context);
        String subCase  = descriptor.hasSubCase() ? descriptor.subCase() : "EQUALS";

        // Header assertions
        if ("HEADER".equals(subCase)) {
            String headerVal = response.getHeader(path);
            if (expected == null || expected.isBlank()) {
                // Just check header exists
                return headerVal != null
                        ? ExecutionResult.success()
                        : ExecutionResult.failure("ASSERT_RESPONSE:HEADER — header not found: " + path);
            }
            return (headerVal != null && headerVal.contains(expected))
                    ? ExecutionResult.success()
                    : ExecutionResult.failure(
                            "ASSERT_RESPONSE:HEADER [" + path + "] failed — actual: ["
                                    + headerVal + "] expected to contain: [" + expected + "]"
                    );
        }

        // Body path assertions
        String actual = JsonPathExtractor.extract(response.body(), path);

        return switch (subCase) {
            case "NOT_NULL"    -> (actual != null)
                    ? ExecutionResult.success()
                    : ExecutionResult.failure("ASSERT_RESPONSE:NOT_NULL — [" + path + "] is null or absent");

            case "NULL"        -> (actual == null)
                    ? ExecutionResult.success()
                    : ExecutionResult.failure("ASSERT_RESPONSE:NULL — [" + path + "] has value: " + actual);

            case "CONTAINS"    -> (actual != null && actual.contains(expected))
                    ? ExecutionResult.success()
                    : ExecutionResult.failure(
                            "ASSERT_RESPONSE:CONTAINS [" + path + "] — actual: [" + actual
                                    + "] does not contain: [" + expected + "]");

            case "NOT_EQUALS"  -> (actual != null && !actual.equals(expected))
                    ? ExecutionResult.success()
                    : ExecutionResult.failure(
                            "ASSERT_RESPONSE:NOT_EQUALS [" + path + "] — actual equals expected: " + expected);

            default            -> { // EQUALS
                if (actual == null) {
                    yield ExecutionResult.failure(
                            "ASSERT_RESPONSE [" + path + "] — path not found in response body");
                }
                yield actual.equals(expected)
                        ? ExecutionResult.success()
                        : ExecutionResult.failure(
                                "ASSERT_RESPONSE [" + path + "] failed — expected: ["
                                        + expected + "] actual: [" + actual + "]");
            }
        };
    }

    private String resolveExpected(Step step, ExecutionContext context) {
        String raw = step.getKey();
        if (raw == null) return null;
        Object resolved = context.resolveValue(raw);
        return resolved == null ? null : resolved.toString();
    }

    private ApiResponse getLastResponse(ExecutionContext context) {
        return context.getDataStore()
                .get(SendRequestAction.LAST_RESPONSE_KEY)
                .filter(v -> v instanceof ApiResponse)
                .map(v -> (ApiResponse) v)
                .orElse(null);
    }
}
