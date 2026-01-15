package com.hag.core.engine.result.db;

import com.hag.core.engine.result.ExecutionResult;

import java.util.List;
import java.util.Map;

public class DbExecutionResult extends ExecutionResult {

    public static final String ROWS = "rows";

    public DbExecutionResult(List<Map<String, Object>> rows) {
        super(Map.of(ROWS, rows));
    }

    public List<Map<String, Object>> getRows() {
        return (List<Map<String, Object>>) getData().get(ROWS);
    }
}
