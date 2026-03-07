package com.hag.api.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Immutable representation of an HTTP API request — built from a JSON template
 * after variable substitution.
 *
 * <h3>Template format (JSON)</h3>
 * <pre>
 * {
 *   "_method":   "POST",
 *   "_endpoint": "/api/v1/users",
 *   "_headers":  { "Content-Type": "application/json" },
 *   "username":  "${username}",
 *   "password":  "${password}"
 * }
 * </pre>
 *
 * <p>Fields prefixed with {@code _} are control fields consumed by H-A-G.
 * All other fields form the request body.
 */
public final class ApiRequest {

    private final String              method;
    private final String              endpoint;
    private final Map<String, String> headers;
    private final String              body;         // serialised JSON body (non-_ fields)
    private final Map<String, Object> rawBody;      // structured body for RestAssured
    private final String              contentType;

    public ApiRequest(
            String              method,
            String              endpoint,
            Map<String, String> headers,
            String              body,
            Map<String, Object> rawBody
    ) {
        this.method      = method.toUpperCase();
        this.endpoint    = endpoint;
        this.headers     = headers  != null ? Collections.unmodifiableMap(new HashMap<>(headers))  : Collections.emptyMap();
        this.body        = body;
        this.rawBody     = rawBody  != null ? Collections.unmodifiableMap(new HashMap<>(rawBody))  : Collections.emptyMap();
        this.contentType = headers  != null ? headers.getOrDefault("Content-Type", "application/json") : "application/json";
    }

    public String              method()      { return method;      }
    public String              endpoint()    { return endpoint;    }
    public Map<String, String> headers()     { return headers;     }
    public String              body()        { return body;        }
    public Map<String, Object> rawBody()     { return rawBody;     }
    public String              contentType() { return contentType; }

    @Override
    public String toString() {
        return "ApiRequest{method=" + method + ", endpoint=" + endpoint + "}";
    }
}
