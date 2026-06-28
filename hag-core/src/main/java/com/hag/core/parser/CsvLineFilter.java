package com.hag.core.parser;

/**
 * Classifies raw CSV lines before they reach {@link CsvRowMapper}.
 *
 * <h3>Line types</h3>
 * <ul>
 *   <li><b>Directive</b>  — starts with {@code #!} → carry metadata, routed to
 *       {@link Directive#parse(String)}, never ignored</li>
 *   <li><b>Comment</b>    — starts with {@code #} (but NOT {@code #!}) → ignored</li>
 *   <li><b>Blank</b>      — empty or all-commas → ignored</li>
 *   <li><b>Data row</b>   — everything else → mapped by {@link CsvRowMapper}</li>
 * </ul>
 */
public final class CsvLineFilter {

    private CsvLineFilter() {}

    /**
     * Returns {@code true} when the line should be skipped entirely (blank or
     * plain comment). Directive lines ({@code #!}) return {@code false} — they
     * are handled by the caller separately.
     */
    public static boolean isIgnorable(final String line) {
        if (line == null) return true;
        String t = line.trim();
        if (t.isEmpty() || t.replace(",", "").trim().isEmpty()) return true;
        if (t.startsWith("#!")) return false; // directive — NOT ignorable
        if (t.startsWith("#") || t.startsWith("//")) return true; // plain comment
        return false;
    }

    /** Returns {@code true} if the line is a {@code #!} directive. */
    public static boolean isDirective(final String line) {
        if (line == null) return false;
        return line.trim().startsWith("#!");
    }

    /** Returns {@code true} if the line is a plain {@code #} comment (not a directive). */
    public static boolean isComment(final String line) {
        if (line == null) return false;
        String t = line.trim();
        return t.startsWith("#") && !t.startsWith("#!");
    }
}

