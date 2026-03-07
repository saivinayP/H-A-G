package com.hag.core.result;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

public final class ExecutionResult {

    private final boolean success;
    private final String message;
    private final Map<String, Object> data;

    private ExecutionResult(
            boolean success,
            String message,
            Map<String, Object> data
    ) {
        this.success = success;
        this.message = message;
        this.data = data == null
                ? Collections.emptyMap()
                : Collections.unmodifiableMap(data);
    }

    public static ExecutionResult success() {
        return new ExecutionResult(true, null, null);
    }

    public static ExecutionResult success(String message) {
        return new ExecutionResult(true, message, null);
    }

    public static ExecutionResult success(
            Map<String, Object> data
    ) {
        return new ExecutionResult(true, null, data);
    }

    /**
     * Indicates the action intentionally did not handle this step
     * (e.g. a sub-case it does not own). The dispatcher may try
     * another registered action with the same primary name.
     */
    public static ExecutionResult skipped() {
        return new ExecutionResult(true, "SKIPPED", null);
    }

    public static ExecutionResult failure(
            String message
    ) {
        return new ExecutionResult(
                false,
                Objects.requireNonNullElse(
                        message,
                        "Execution failed"
                ),
                null
        );
    }

    public static ExecutionResult failure(
            String message,
            Map<String, Object> data
    ) {
        return new ExecutionResult(
                false,
                Objects.requireNonNullElse(
                        message,
                        "Execution failed"
                ),
                data
        );
    }

    public boolean isSuccess() {
        return success;
    }

    public boolean isFailure() {
        return !success;
    }

    public String getMessage() {
        return message;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public boolean hasData() {
        return !data.isEmpty();
    }
}