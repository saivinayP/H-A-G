package com.hag.core.result.api;

import com.hag.core.result.ExecutionResult;

import java.util.Map;

public final class ApiExecutionResult {

    public static final String RESPONSE = "response";
    public static final String STATUS_CODE = "statusCode";
    public static final String HEADERS = "headers";

    private final ExecutionResult result;

    private ApiExecutionResult(ExecutionResult result) {
        this.result = result;
    }

    public static ApiExecutionResult success(Map<String, Object> data) {
        return new ApiExecutionResult(
                ExecutionResult.success(data)
        );
    }

    public static ApiExecutionResult failure(String message) {
        return new ApiExecutionResult(
                ExecutionResult.failure(message)
        );
    }

    public boolean isSuccess() {
        return result.isSuccess();
    }

    public String getMessage() {
        return result.getMessage();
    }

    public Map<String, Object> getData() {
        return result.getData();
    }

    public Object getResponseBody() {
        return getData().get(RESPONSE);
    }

    public Object getStatusCode() {
        return getData().get(STATUS_CODE);
    }

    public Object getHeaders() {
        return getData().get(HEADERS);
    }

    public ExecutionResult toExecutionResult() {
        return result;
    }
}