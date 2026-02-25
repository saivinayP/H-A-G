package com.hag.core.adapter;

import java.util.Map;

public interface ApiAdapter {

    ApiResponse send(
            String requestPayload,
            Map<String, String> headers,
            String endpointHint
    );
}
