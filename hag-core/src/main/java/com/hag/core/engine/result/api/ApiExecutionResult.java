package com.hag.core.engine.result.api;

import com.hag.core.engine.result.ExecutionResult;

import java.util.Map;

public class ApiExecutionResult extends ExecutionResult {

    public static final String RESPONSE = "response";
    public static final String STATUS_CODE = "statusCode";
    public static final String HEADERS = "headers";

    public ApiExecutionResult(Map<String, Object> data) {
        super(data);
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
}
