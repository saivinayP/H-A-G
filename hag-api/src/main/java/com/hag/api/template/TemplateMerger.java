package com.hag.api.template;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hag.api.model.ApiRequest;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Merges a JSON API request template with variable values from the DataStore.
 *
 * <h3>Template format</h3>
 * <pre>
 * {
 *   "_method":   "POST",
 *   "_endpoint": "/api/v1/auth/login",
 *   "_headers":  { "Accept": "application/json" },
 *   "username":  "${username}",
 *   "password":  "${password}"
 * }
 * </pre>
 *
 * <p>Control fields (prefixed with {@code _}) are extracted and removed
 * from the body. All remaining fields form the request body.
 *
 * <p>Variable substitution uses {@code ${VAR}} and {@code ${SCOPE:VAR}} syntax.
 * Substitution is applied to the raw JSON string before parsing, so it works
 * in both keys and values.
 */
public final class TemplateMerger {

    private static final ObjectMapper MAPPER   = new ObjectMapper();
    private static final Pattern      VAR_PAT  = Pattern.compile("\\$\\{([^}]+)}");

    private TemplateMerger() {}

    /**
     * Loads a template JSON file, resolves {@code ${VAR}} tokens using
     * {@code variables}, and builds an {@link ApiRequest}.
     *
     * @param templatePath absolute path to the template JSON file
     * @param variables    values to substitute — usually from ExecutionContext DataStore
     * @param baseUrl      API base URL used to construct the full endpoint URL
     * @return a populated, ready-to-send {@link ApiRequest}
     */
    public static ApiRequest merge(
            Path templatePath,
            Map<String, Object> variables,
            String baseUrl
    ) {
        String raw = readFile(templatePath);
        String substituted = substituteVariables(raw, variables);

        Map<String, Object> parsed;
        try {
            parsed = MAPPER.readValue(substituted, new TypeReference<>() {});
        } catch (IOException e) {
            throw new RuntimeException(
                    "Failed to parse template after substitution: " + templatePath, e
            );
        }

        // Extract control fields
        String method   = extractString(parsed, "_method",   "GET");
        String endpoint = extractString(parsed, "_endpoint", "");

        @SuppressWarnings("unchecked")
        Map<String, Object> headerRaw = (Map<String, Object>) parsed.remove("_headers");
        Map<String, String> headers = new LinkedHashMap<>();
        if (headerRaw != null) {
            headerRaw.forEach((k, v) -> headers.put(k, v == null ? "" : v.toString()));
        }

        // Ensure Content-Type default
        headers.putIfAbsent("Content-Type", "application/json");

        // Remaining fields = request body
        String bodyJson;
        try {
            bodyJson = MAPPER.writeValueAsString(parsed);
        } catch (IOException e) {
            throw new RuntimeException("Failed to serialise request body", e);
        }

        String fullUrl = buildUrl(baseUrl, endpoint);

        return new ApiRequest(method, fullUrl, headers, bodyJson, parsed);
    }

    /* ------------------------------------------------------------------ */

    /** Replaces ${VAR} and ${SCOPE:VAR} tokens with values from the map. */
    static String substituteVariables(String template, Map<String, Object> vars) {
        if (vars == null || vars.isEmpty()) return template;

        Matcher m = VAR_PAT.matcher(template);
        StringBuilder sb = new StringBuilder();

        while (m.find()) {
            String token = m.group(1);                    // e.g. "username" or "API:token"
            Object value = resolveToken(token, vars);
            String replacement = value == null ? "" : Matcher.quoteReplacement(value.toString());
            m.appendReplacement(sb, replacement);
        }
        m.appendTail(sb);
        return sb.toString();
    }

    /**
     * Resolves a token against the variable map.
     * Tries exact match first, then strips scope prefix (e.g. {@code API:userId}).
     */
    private static Object resolveToken(String token, Map<String, Object> vars) {
        if (vars.containsKey(token)) return vars.get(token);

        // Try without scope prefix: "API:userId" → "userId"
        int colon = token.indexOf(':');
        if (colon >= 0) {
            String bare = token.substring(colon + 1);
            if (vars.containsKey(bare)) return vars.get(bare);
        }
        return null;
    }

    private static String extractString(Map<String, Object> map, String key, String defaultValue) {
        Object v = map.remove(key);
        return (v != null) ? v.toString() : defaultValue;
    }

    private static String buildUrl(String baseUrl, String endpoint) {
        if (baseUrl == null || baseUrl.isBlank()) return endpoint;
        if (endpoint.startsWith("http://") || endpoint.startsWith("https://")) return endpoint;
        if (baseUrl.endsWith("/") && endpoint.startsWith("/")) return baseUrl + endpoint.substring(1);
        if (!baseUrl.endsWith("/") && !endpoint.startsWith("/")) return baseUrl + "/" + endpoint;
        return baseUrl + endpoint;
    }

    private static String readFile(Path path) {
        try {
            return Files.readString(path, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Cannot read template file: " + path, e);
        }
    }
}
