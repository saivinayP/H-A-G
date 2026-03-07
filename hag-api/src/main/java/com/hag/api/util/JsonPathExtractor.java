package com.hag.api.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Extracts values from a JSON string using dot-notation JSON path expressions.
 *
 * <h3>Supported syntax</h3>
 * <pre>
 *   data.token           → {"data": {"token": "abc"}}
 *   data.user.id         → nested object
 *   items[0].name        → first element of an array field
 *   status               → top-level field
 * </pre>
 *
 * <p>This is a lightweight path resolver — not a full JSONPath implementation.
 * For advanced use cases (filters, wildcards) the full Jayway JSONPath library
 * can be added later.
 */
public final class JsonPathExtractor {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private JsonPathExtractor() {}

    /**
     * Extracts the value at {@code path} from the JSON {@code body}.
     *
     * @param body JSON string
     * @param path dot-notation path, e.g. {@code data.token} or {@code items[0].name}
     * @return the extracted value as a String, or {@code null} if not found
     */
    public static String extract(String body, String path) {

        if (body == null || body.isBlank() || path == null || path.isBlank()) {
            return null;
        }

        try {
            JsonNode root  = MAPPER.readTree(body);
            JsonNode node  = navigate(root, path);
            return node == null || node.isNull() ? null : node.asText();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Returns {@code true} when the node at {@code path} exists and is non-null.
     */
    public static boolean exists(String body, String path) {
        if (body == null || body.isBlank() || path == null) return false;
        try {
            JsonNode root = MAPPER.readTree(body);
            JsonNode node = navigate(root, path);
            return node != null && !node.isNull();
        } catch (Exception e) {
            return false;
        }
    }

    /* ------------------------------------------------------------------ */

    /**
     * Walks the JSON tree segment by segment.
     * Segments are split on {@code .}; array indices like {@code items[0]}
     * are handled by splitting on {@code [}.
     */
    private static JsonNode navigate(JsonNode node, String path) {
        String[] segments = path.split("\\.");

        for (String segment : segments) {
            if (node == null || node.isNull()) return null;

            if (segment.contains("[")) {
                // e.g. "items[0]"
                int bracketOpen  = segment.indexOf('[');
                int bracketClose = segment.indexOf(']');

                if (bracketClose <= bracketOpen) return null;

                String fieldName = segment.substring(0, bracketOpen);
                int    index     = Integer.parseInt(
                        segment.substring(bracketOpen + 1, bracketClose)
                );

                if (!fieldName.isBlank()) {
                    node = node.get(fieldName);
                }
                if (node == null || !node.isArray()) return null;
                node = node.get(index);

            } else {
                node = node.get(segment);
            }
        }
        return node;
    }
}
