package com.hag.core.parser;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Structured metadata extracted from {@code #!} directives at the top of a
 * CSV test file (and optionally scattered between steps).
 *
 * <p>All fields are optional. Unset fields return sensible defaults so that
 * callers never need to null-check for routine access.
 *
 * <h3>Supported directives</h3>
 * <pre>
 *   #!NAME:        Human-readable test name shown in reports
 *   #!TAGS:        Comma-separated tag/group list (maps to TestNG groups)
 *   #!PRIORITY:    critical | high | medium | low
 *   #!AUTHOR:      Team or person name
 *   #!TIMEOUT:     Total test timeout in seconds (integer)
 *   #!RETRY:       Full-test retry count (integer)
 *   #!DRIVER:      web | mobile | api-only | db-only
 *   #!ENVIRONMENT: Comma-separated list of environments this test runs on
 *   #!DESCRIPTION: Free-text description for report tooltips
 *   #!SKIP:        true | false
 * </pre>
 */
public final class TestMetadata {

    private String       name;
    private List<String> tags        = Collections.emptyList();
    private String       priority    = "medium";
    private String       author;
    private int          timeout     = -1;    // -1 = use global config
    private int          retry       = -1;    // -1 = use global config
    private String       driver      = "web";
    private List<String> environments = Collections.emptyList();
    private String       description;
    private boolean      skip        = false;

    private TestMetadata() {}

    /** Returns a new empty {@link TestMetadata} — callers apply directives via {@link #apply}. */
    public static TestMetadata empty() {
        return new TestMetadata();
    }

    /**
     * Applies a single parsed {@link Directive} to this metadata object.
     * Unknown directive keys are silently ignored.
     *
     * @param directive the directive to process
     */
    public void apply(final Directive directive) {
        switch (directive.key()) {
            case "NAME"        -> name        = directive.value();
            case "TAGS"        -> tags        = splitCsv(directive.value());
            case "PRIORITY"    -> priority    = directive.value().toLowerCase();
            case "AUTHOR"      -> author      = directive.value();
            case "TIMEOUT"     -> timeout     = parseIntSafe(directive.value(), -1);
            case "RETRY"       -> retry       = parseIntSafe(directive.value(), -1);
            case "DRIVER"      -> driver      = directive.value().toLowerCase();
            case "ENVIRONMENT" -> environments = splitCsv(directive.value());
            case "DESCRIPTION" -> description = directive.value();
            case "SKIP"        -> skip        = Boolean.parseBoolean(directive.value());
            default            -> { /* unknown directive — ignored */ }
        }
    }

    // ── Getters ──────────────────────────────────────────────────────────────

    public String       getName()         { return name;         }
    public List<String> getTags()         { return tags;         }
    public String       getPriority()     { return priority;     }
    public String       getAuthor()       { return author;       }
    public int          getTimeout()      { return timeout;      }
    public int          getRetry()        { return retry;        }
    public String       getDriver()       { return driver;       }
    public List<String> getEnvironments() { return environments; }
    public String       getDescription()  { return description;  }
    public boolean      isSkip()          { return skip;         }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private static List<String> splitCsv(final String value) {
        if (value == null || value.isBlank()) return Collections.emptyList();
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }

    private static int parseIntSafe(final String value, final int fallback) {
        try {
            return Integer.parseInt(value.strip());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    @Override
    public String toString() {
        return "TestMetadata{name='" + name + "', tags=" + tags
                + ", priority='" + priority + "', driver='" + driver + "'}";
    }
}
