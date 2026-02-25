package com.hag.core.result.db;

import com.hag.core.result.ExecutionResult;

import java.util.List;
import java.util.Map;

public final class DbExecutionResult {

    public static final String ROWS = "rows";

    private final ExecutionResult result;

    private DbExecutionResult(ExecutionResult result) {
        this.result = result;
    }

    public static DbExecutionResult success(
            List<Map<String, Object>> rows
    ) {
        return new DbExecutionResult(
                ExecutionResult.success(
                        Map.of(ROWS, rows)
                )
        );
    }

    public static DbExecutionResult failure(String message) {
        return new DbExecutionResult(
                ExecutionResult.failure(message)
        );
    }

    public boolean isSuccess() {
        return result.isSuccess();
    }

    public String getMessage() {
        return result.getMessage();
    }

    public List<Map<String, Object>> getRows() {
        return (List<Map<String, Object>>)
                result.getData().get(ROWS);
    }

    public ExecutionResult toExecutionResult() {
        return result;
    }
}