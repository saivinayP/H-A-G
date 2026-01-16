package com.hag.core.engine.adapter;

import java.util.Map;

public class ApiResponse {

    private final int statusCode;
    private final String body;
    private final Map<String, String> headers;

    public ApiResponse(int statusCode, String body, Map<String, String> headers) {
        this.statusCode = statusCode;
        this.body = body;
        this.headers = headers;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getBody() {
        return body;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }
}
