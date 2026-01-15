package com.hag.core.engine.result;

import java.util.Collections;
import java.util.Map;

public class ExecutionResult {

    private final Map<String, Object> data;

    public ExecutionResult(Map<String, Object> data) {
        this.data = data == null ? Collections.emptyMap() : data;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public boolean isEmpty() {
        return data.isEmpty();
    }
}
