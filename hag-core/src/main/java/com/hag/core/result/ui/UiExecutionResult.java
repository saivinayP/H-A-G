package com.hag.core.result.ui;

import com.hag.core.result.ExecutionResult;

import java.util.Map;

public final class UiExecutionResult {

    public static final String TEXT = "text";
    public static final String ATTRIBUTES = "attributes";

    private final ExecutionResult result;

    private UiExecutionResult(ExecutionResult result) {
        this.result = result;
    }

    public static UiExecutionResult success(Map<String, Object> data) {
        return new UiExecutionResult(
                ExecutionResult.success(data)
        );
    }

    public static UiExecutionResult failure(String message) {
        return new UiExecutionResult(
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

    public Object getText() {
        return getData().get(TEXT);
    }

    public Object getAttribute(String name) {
        Map<String, Object> attributes =
                (Map<String, Object>) getData().get(ATTRIBUTES);

        return attributes != null
                ? attributes.get(name)
                : null;
    }

    public ExecutionResult toExecutionResult() {
        return result;
    }
}