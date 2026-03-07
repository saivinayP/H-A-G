package com.hag.api.action;

import com.hag.api.model.ApiResponse;
import com.hag.api.util.JsonPathExtractor;
import com.hag.core.context.DataScope;
import com.hag.core.context.ExecutionContext;
import com.hag.core.dispatcher.descriptor.ActionDescriptor;
import com.hag.core.executor.Action;
import com.hag.core.executor.ActionCategory;
import com.hag.core.model.Step;
import com.hag.core.result.ExecutionResult;

/**
 * STORE_DATA:RESPONSE — extracts a value from the last API response body and stores it.
 * STORE_DATA:HEADER   — extracts a response header value and stores it.
 * STORE_DATA:STATUS   — stores the response HTTP status code.
 *
 * <h3>CSV Syntax</h3>
 * <pre>
 *   STORE_DATA:RESPONSE,data.token,,authToken        → store JSON path value as 'authToken'
 *   STORE_DATA:HEADER,Content-Type,,contentType      → store header value as 'contentType'
 *   STORE_DATA:STATUS,,,lastStatus                   → store HTTP status code as 'lastStatus'
 * </pre>
 *
 * <ul>
 *   <li><b>Recipient</b> — JSON path (RESPONSE) or header name (HEADER)</li>
 *   <li><b>Key</b>       — variable name to store into (in API scope)</li>
 * </ul>
 */
public final class StoreDataResponseAction implements Action {

    @Override
    public String name() { return "STORE_DATA"; }

    @Override
    public ActionCategory category() { return ActionCategory.API; }

    @Override
    public ExecutionResult execute(Step step, ActionDescriptor descriptor, ExecutionContext context) {

        if (!descriptor.hasSubCase()) {
            // Defer to the UI STORE_DATA handled elsewhere — this action only handles API sub-cases
            return ExecutionResult.skipped();
        }

        String subCase = descriptor.subCase();
        if (!subCase.equals("RESPONSE") && !subCase.equals("HEADER") && !subCase.equals("STATUS")) {
            return ExecutionResult.skipped();
        }

        ApiResponse response = getLastResponse(context);
        if (response == null) {
            return ExecutionResult.failure(
                    "STORE_DATA:" + subCase + " — no API response found. Run SEND_REQUEST first."
            );
        }

        String variableName = step.getKey();
        if (variableName == null || variableName.isBlank()) {
            return ExecutionResult.failure(
                    "STORE_DATA:" + subCase + " requires variable name in Key column"
            );
        }

        String value = switch (subCase) {
            case "RESPONSE" -> {
                String path = step.getRecipient();
                if (path == null || path.isBlank()) {
                    yield response.body();   // store entire body
                }
                yield JsonPathExtractor.extract(response.body(), path);
            }
            case "HEADER" -> {
                String headerName = step.getRecipient();
                yield (headerName != null) ? response.getHeader(headerName) : null;
            }
            case "STATUS" -> String.valueOf(response.statusCode());
            default -> null;
        };

        context.getDataStore().put(DataScope.API, variableName, value);
        return ExecutionResult.success("Stored [" + variableName + "] = " + value);
    }

    private ApiResponse getLastResponse(ExecutionContext context) {
        return context.getDataStore()
                .get(DataScope.GLOBAL, SendRequestAction.LAST_RESPONSE_KEY)
                .filter(v -> v instanceof ApiResponse)
                .map(v -> (ApiResponse) v)
                .orElse(null);
    }
}
