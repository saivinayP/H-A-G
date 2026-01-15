package com.hag.core.engine.result.ui;

import com.hag.core.engine.result.ExecutionResult;

import java.util.Map;

public class UiExecutionResult extends ExecutionResult {

    public static final String TEXT = "text";
    public static final String ATTRIBUTES = "attributes";

    public UiExecutionResult(Map<String, Object> data) {
        super(data);
    }

    public Object getText() {
        return getData().get(TEXT);
    }

    public Object getAttribute(String name) {
        return ((Map<String, Object>) getData().get(ATTRIBUTES)).get(name);
    }
}
