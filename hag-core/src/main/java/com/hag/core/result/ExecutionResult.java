package com.hag.core.result;

import java.util.Collections;
import java.util.Map;

public class ExecutionResult {

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
                : data;
    }

    /* =========================
       Static factory methods
       ========================= */

    public static ExecutionResult success() {
        return new ExecutionResult(true, null, null);
    }

    public static ExecutionResult success(Map<String, Object> data) {
        return new ExecutionResult(true, null, data);
    }

    public static ExecutionResult failure(String message) {
        return new ExecutionResult(false, message, null);
    }

    public static ExecutionResult failure(
            String message,
            Map<String, Object> data
    ) {
        return new ExecutionResult(false, message, data);
    }

    /* =========================
       Instance methods
       ========================= */

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public boolean isEmpty() {
        return data.isEmpty();
    }
}