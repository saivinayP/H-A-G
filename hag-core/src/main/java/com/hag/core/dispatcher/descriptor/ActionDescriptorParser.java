package com.hag.core.dispatcher.descriptor;

import java.util.*;

/**
 * Parses the Action CSV column into an {@link ActionDescriptor}.
 *
 * <h3>Action column syntax</h3>
 * <pre>
 *   ACTION
 *   ACTION:SUBCASE
 *   ACTION|opt=val|opt=val
 *   ACTION:SUBCASE|opt=val|opt=val
 * </pre>
 *
 * <ul>
 *   <li>{@code ACTION}  — primary action name, e.g. {@code CLICK}, {@code WAIT}</li>
 *   <li>{@code SUBCASE} — optional variant after the first colon,
 *       e.g. {@code DOUBLE} giving {@code CLICK:DOUBLE}</li>
 *   <li>{@code |opts}   — optional per-step execution options after the first pipe,
 *       parsed into a {@link StepOptions} record</li>
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
     * Parses the Action column value, which may contain:
     * {@code ACTION}, {@code ACTION:SUBCASE}, {@code ACTION|opts},
     * or {@code ACTION:SUBCASE|opts}.
     *
     * <p>The optional {@code |opts} portion is extracted into a {@link StepOptions}
     * record and attached to the returned {@link ActionDescriptor}.
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

        // ── Split off per-step options at the first pipe ──────────────────
        final String actionPart;
        final StepOptions stepOptions;

        int firstPipe = rawAction.indexOf('|');
        if (firstPipe >= 0) {
            actionPart  = rawAction.substring(0, firstPipe).trim().toUpperCase();
            stepOptions = StepOptions.parse(rawAction.substring(firstPipe + 1));
        } else {
            actionPart  = rawAction.trim().toUpperCase();
            stepOptions = StepOptions.defaults();
        }

        // ── Parse ACTION and optional :SUBCASE ───────────────────────────
        int colon = actionPart.indexOf(':');

        if (colon < 0) {
            // Simple action — no sub-case
            return new ActionDescriptor(
                    actionPart,
                    null,
                    Collections.emptySet(),
                    Collections.emptyMap(),
                    stepOptions
            );
        }

        String name    = actionPart.substring(0, colon).strip();
        String subCase = actionPart.substring(colon + 1).strip();

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
                Collections.emptyMap(),
                stepOptions
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
     *       known file extension ({@code .json}, {@code .xml}, {@code .sql},
     *       {@code .csv})</li>
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
                || token.endsWith(".sql")
                || token.endsWith(".csv");
    }
}