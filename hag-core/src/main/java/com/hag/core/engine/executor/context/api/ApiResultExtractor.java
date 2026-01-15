package com.hag.core.engine.executor.context.api;

import java.util.List;
import java.util.Map;

public final class ApiResultExtractor {

    private ApiResultExtractor() {}

    @SuppressWarnings("unchecked")
    public static Object extract(
            Object response,
            Object statusCode,
            Object headers,
            String path
    ) {

        if ("statusCode".equals(path)) {
            return statusCode;
        }

        if (path.startsWith("headers.")) {
            String headerKey = path.substring("headers.".length());
            return ((Map<String, Object>) headers).get(headerKey);
        }

        if (!path.startsWith("response.")) {
            throw new IllegalArgumentException(
                    "Invalid API extraction path: " + path
            );
        }

        String expr = path.substring("response.".length());
        String[] parts = expr.split("\\.");

        Object current = response;

        for (String part : parts) {
            current = resolveNode(current, part);
        }

        return current;
    }

    private static Object resolveNode(Object current, String token) {

        if (current instanceof Map<?, ?> map) {
            return map.get(token);
        }

        if (current instanceof List<?> list && token.matches("\\d+")) {
            return list.get(Integer.parseInt(token));
        }

        if (token.contains("[") && token.endsWith("]")) {
            String key = token.substring(0, token.indexOf("["));
            int index = Integer.parseInt(
                    token.substring(token.indexOf("[") + 1, token.length() - 1)
            );
            Object value = ((Map<String, Object>) current).get(key);
            return ((List<?>) value).get(index);
        }

        throw new IllegalStateException("Invalid API path token: " + token);
    }
}
