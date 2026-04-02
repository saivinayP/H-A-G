package com.hag.core.parser;

/**
 * A machine-readable directive parsed from a {@code #!} prefixed line.
 *
 * <h3>Line format</h3>
 * <pre>
 *   #!KEY: value
 *   #!KEY:value
 *   #!KEY:            (empty value allowed)
 * </pre>
 *
 * <p>The {@code #!} prefix distinguishes directives from plain comments ({@code #}).
 * Plain comments are completely ignored by the parser; directives carry metadata
 * consumed by the engine and report layer.
 */
public record Directive(String key, String value) {

    /**
     * Parses one {@code #!} prefixed line into a {@link Directive}.
     *
     * @param line the full raw line including the {@code #!} prefix
     * @return a {@link Directive} with an upper-cased key and trimmed value
     * @throws IllegalArgumentException if the line does not start with {@code #!}
     */
    public static Directive parse(final String line) {

        if (line == null || !line.stripLeading().startsWith("#!")) {
            throw new IllegalArgumentException(
                    "Directive line must start with '#!': " + line
            );
        }

        // Strip the "#!" prefix
        String body = line.stripLeading().substring(2).trim();

        int colon = body.indexOf(':');
        if (colon < 0) {
            // Key only, no value
            return new Directive(body.trim().toUpperCase(), "");
        }

        String key   = body.substring(0, colon).trim().toUpperCase();
        String value = body.substring(colon + 1).trim();
        return new Directive(key, value);
    }

    /** Returns {@code true} when this directive has a non-blank value. */
    public boolean hasValue() {
        return value != null && !value.isBlank();
    }
}
