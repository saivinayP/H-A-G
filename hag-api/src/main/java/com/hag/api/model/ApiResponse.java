package com.hag.api.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Immutable representation of an HTTP API response.
 *
 * <p>Wraps the RestAssured {@code Response} into a neutral model so that
 * assertion and store actions don't depend on RestAssured types directly.
 */
public final class ApiResponse {

    private final int                 statusCode;
    private final Map<String, String> headers;
    private final String              body;        // raw response body as string

    public ApiResponse(int statusCode, Map<String, String> headers, String body) {
        this.statusCode = statusCode;
        this.headers    = headers != null
                ? Collections.unmodifiableMap(new HashMap<>(headers))
                : Collections.emptyMap();
        this.body       = body != null ? body : "";
    }

    public int                 statusCode() { return statusCode; }
    public Map<String, String> headers()    { return headers;    }
    public String              body()       { return body;       }

    /**
     * Returns the value of a named response header, or {@code null} if absent.
     * Header name lookup is case-insensitive.
     */
    public String getHeader(String name) {
        return headers.entrySet().stream()
                .filter(e -> e.getKey().equalsIgnoreCase(name))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(null);
    }

    @Override
    public String toString() {
        return "ApiResponse{status=" + statusCode + ", body-length=" + body.length() + "}";
    }
}
