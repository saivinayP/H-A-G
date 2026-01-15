package com.hag.core.engine.executor.context.api;

import java.util.Map;

public final class ApiResultExtractor {

    private ApiResultExtractor() {}

    @SuppressWarnings("unchecked")
    public static Object extract(Object response, String path) {

        if (response == null) {
            throw new IllegalStateException("API response is null");
        }

        String[] parts = path.split("\\.");
        Object current = response;

        for (String part : parts) {
            if (!(current instanceof Map)) {
                throw new IllegalStateException(
                        "Cannot extract path '" + path + "' from non-object node"
                );
            }
            current = ((Map<String, Object>) current).get(part);
            if (current == null) {
                throw new IllegalStateException(
                        "Path '" + path + "' not found in API response"
                );
            }
        }
        return current;
    }
}
