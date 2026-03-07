package com.hag.core.dispatcher.descriptor;

import java.util.*;

/**
 * Parses the Action CSV column into an {@link ActionDescriptor}.
 *
 * <h3>Action column syntax</h3>
 * <pre>
 *   ACTION
 *   ACTION:SUBCASE
 * </pre>
 *
 * <ul>
 *   <li>{@code ACTION}  — primary action name, e.g. {@code CLICK}, {@code WAIT}</li>
 *   <li>{@code SUBCASE} — optional variant after the first colon,
 *       e.g. {@code DOUBLE} giving {@code CLICK:DOUBLE}</li>
 * </ul>
 *
 * <h3>Modifier / flag syntax (Source column)</h3>
 *
 * When the Source column is not a test-data file path, it carries pipe-separated
 * modifier flags and key=value parameters:
 * <pre>
 *   trim|upper              → flags: {trim, upper}
 *   timeout=30|scope=UI     → params: {timeout=30, scope=UI}
 *   clear                   → flags: {clear}
 * </pre>
 *
 * <p><b>No commas</b> are ever used inside the Action column — this avoids
 * any conflict with the four-column CSV delimiter.
 */
public final class ActionDescriptorParser {

    private ActionDescriptorParser() {}

    /* ------------------------------------------------------------------
       Action column parsing
       ------------------------------------------------------------------ */

    /**
     * Parses the Action column value {@code ACTION} or {@code ACTION:SUBCASE}.
     *
     * @param rawAction raw value from the Action CSV column
     * @return a fully populated {@link ActionDescriptor}
     * @throws IllegalArgumentException if the value is null/blank or malformed
     */
    public static ActionDescriptor parse(final String rawAction) {

        if (rawAction == null || rawAction.isBlank()) {
            throw new IllegalArgumentException(
                    "Action cannot be null or blank"
            );
        }

        String trimmed = rawAction.trim().toUpperCase();
        int colon = trimmed.indexOf(':');

        if (colon < 0) {
            // Simple action — no sub-case
            return new ActionDescriptor(
                    trimmed,
                    null,
                    Collections.emptySet(),
                    Collections.emptyMap()
            );
        }

        String name    = trimmed.substring(0, colon).strip();
        String subCase = trimmed.substring(colon + 1).strip();

        if (name.isBlank()) {
            throw new IllegalArgumentException(
                    "Action name must not be blank in: [" + rawAction + "]"
            );
        }
        if (subCase.isBlank()) {
            throw new IllegalArgumentException(
                    "Sub-case must not be blank after ':' in: [" + rawAction + "]"
            );
        }

        return new ActionDescriptor(
                name,
                subCase,
                Collections.emptySet(),
                Collections.emptyMap()
        );
    }

    /* ------------------------------------------------------------------
       Source column modifier parsing
       ------------------------------------------------------------------ */

    /**
     * Parses pipe-separated modifier tokens from the Source CSV column.
     *
     * <p>Each {@code |}-delimited token is classified as:
     * <ul>
     *   <li><b>Parameter</b> — token contains {@code =}  (e.g. {@code timeout=30})</li>
     *   <li><b>File path</b> — token contains {@code /}, {@code \}, or ends with a
     *       known file extension ({@code .json}, {@code .xml}, {@code .sql})</li>
     *   <li><b>Flag</b>     — everything else (e.g. {@code clear}, {@code trim})</li>
     * </ul>
     *
     * @param source raw value from the Source CSV column
     * @return a {@link ModifierSet} (never {@code null})
     */
    public static ModifierSet parseModifiers(final String source) {

        if (source == null || source.isBlank()) {
            return ModifierSet.empty();
        }

        Set<String>         flags      = new LinkedHashSet<>();
        Map<String, String> parameters = new LinkedHashMap<>();
        List<String>        filePaths  = new ArrayList<>();

        for (String raw : source.split("\\|")) {
            String token = raw.strip();
            if (token.isEmpty()) continue;

            if (isFilePath(token)) {
                filePaths.add(token);
            } else if (token.contains("=")) {
                String[] kv = token.split("=", 2);
                parameters.put(kv[0].strip().toLowerCase(), kv[1].strip());
            } else {
                flags.add(token.toLowerCase());
            }
        }

        return new ModifierSet(flags, parameters, filePaths);
    }

    /** A token is a file path if it contains a path separator or known extension. */
    private static boolean isFilePath(final String token) {
        return token.contains("/")
                || token.contains("\\")
                || token.endsWith(".json")
                || token.endsWith(".xml")
                || token.endsWith(".sql");
    }
}