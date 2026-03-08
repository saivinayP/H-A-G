package com.hag.api.adapter;

import com.hag.api.model.ApiRequest;
import com.hag.api.model.ApiResponse;
import com.hag.core.adapter.ApiAdapter;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * RestAssured-based implementation of {@link ApiAdapter}.
 *
 * <p>Accepts an {@link ApiRequest} and returns an {@link ApiResponse}.
 * Supports GET, POST, PUT, PATCH, DELETE, and HEAD methods.
 *
 * <p>SSL validation is relaxed by default for test environments.
 */
public final class RestAssuredApiAdapter implements ApiAdapter {

    private static final Logger       LOG          = LoggerFactory.getLogger(RestAssuredApiAdapter.class);
    private static final Set<String>  BODY_METHODS = Set.of("POST", "PUT", "PATCH");

    private boolean relaxedHttps;

    public RestAssuredApiAdapter() {
        this(true);
    }

    public RestAssuredApiAdapter(boolean relaxedHttps) {
        this.relaxedHttps = relaxedHttps;
    }

    public void setRelaxedHttps(boolean relaxedHttps) {
        this.relaxedHttps = relaxedHttps;
    }

    /**
     * Executes the given {@link ApiRequest} and returns an {@link ApiResponse}.
     *
     * @param request an {@link ApiRequest} instance
     * @return an {@link ApiResponse} — never {@code null}
     */
    @Override
    public Object execute(Object request) {
        if (!(request instanceof ApiRequest apiRequest)) {
            throw new IllegalArgumentException(
                    "RestAssuredApiAdapter expects ApiRequest, got: "
                            + (request == null ? "null" : request.getClass().getName())
            );
        }
        return send(apiRequest);
    }

    public ApiResponse send(ApiRequest req) {

        LOG.info("API → {} {}", req.method(), req.endpoint());

        RequestSpecification spec = RestAssured.given().log().uri();

        if (relaxedHttps) {
            spec = spec.relaxedHTTPSValidation();
        }

        if (!req.headers().isEmpty()) {
            spec = spec.headers(new HashMap<>(req.headers()));
        }

        String method  = req.method();
        boolean hasBody = BODY_METHODS.contains(method)
                && req.body() != null
                && !req.body().equals("{}");

        if (hasBody) {
            spec = spec.body(req.body());
        }

        Response raw = switch (method) {
            case "GET"    -> spec.get(req.endpoint());
            case "POST"   -> spec.post(req.endpoint());
            case "PUT"    -> spec.put(req.endpoint());
            case "PATCH"  -> spec.patch(req.endpoint());
            case "DELETE" -> spec.delete(req.endpoint());
            case "HEAD"   -> spec.head(req.endpoint());
            default       -> throw new IllegalArgumentException("Unsupported HTTP method: " + method);
        };

        Map<String, String> responseHeaders = new HashMap<>();
        raw.headers().forEach(h -> responseHeaders.put(h.getName(), h.getValue()));

        String body = raw.getBody().asString();
        LOG.info("API ← {} (body-length={})", raw.getStatusCode(), body.length());
        LOG.debug("API ← Body: {}", body);

        return new ApiResponse(raw.getStatusCode(), responseHeaders, body);
    }
}
