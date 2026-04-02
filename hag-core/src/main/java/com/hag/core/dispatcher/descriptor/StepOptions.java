package com.hag.core.dispatcher.descriptor;

/**
 * Per-step execution control options extracted from the {@code Action} column
 * pipe-suffix.
 *
 * <h3>Syntax</h3>
 * <pre>
 *   ACTION:SUBCASE|onfail=continue|timeout=60|masked=true
 * </pre>
 *
 * <p>Only options present in the raw string override the configured default.
 * Fields left at their sentinel value ({@code -1} / {@code null} / {@code false})
 * tell the engine to fall back to its global configuration.
 */
public record StepOptions(

        /**
         * Failure behaviour for this step.
         * One of: {@code stop} (default), {@code continue}, {@code warn},
         * {@code retry:N} (stored as-is, engine parses the count).
         */
        String onFail,

        /**
         * Step-level timeout override in seconds.
         * {@code -1} = use global config value.
         */
        int timeout,

        /**
         * Step-level retry count override.
         * {@code -1} = use global config value.
         */
        int retry,

        /**
         * When {@code true}, the Key column value is hidden in logs and reports.
         * Used for passwords and secrets.
         */
        boolean masked,

        /**
         * When {@code true}, a screenshot is captured after this step regardless
         * of the global screenshot configuration.
         * When {@code false} explicitly set, suppresses even a global "always" policy.
         * {@code null} = defer to global config.
         */
        Boolean screenshot,

        /**
         * Milliseconds to wait before executing this step. {@code 0} = no delay.
         */
        int delayMs,

        /**
         * Driver session routing hint. {@code "web"} (default), {@code "mobile"}.
         * {@code null} = use the primary driver.
         */
        String driver

) {

    /** All defaults — no overrides applied. */
    public static StepOptions defaults() {
        return new StepOptions("stop", -1, -1, false, null, 0, null);
    }

    /**
     * Parses the pipe-separated options appended to the Action column after
     * the first {@code |} character.
     *
     * <p>Example input: {@code onfail=continue|timeout=60|masked=true}
     *
     * @param raw the options string (everything after the first {@code |} in the Action cell)
     * @return a fully resolved {@link StepOptions}; falls back to defaults for missing keys
     */
    public static StepOptions parse(final String raw) {

        if (raw == null || raw.isBlank()) {
            return defaults();
        }

        String onFail    = "stop";
        int    timeout   = -1;
        int    retry     = -1;
        boolean masked   = false;
        Boolean screenshot = null;
        int    delayMs   = 0;
        String driver    = null;

        for (String token : raw.split("\\|")) {
            String t = token.strip();
            if (t.isEmpty()) continue;

            if (!t.contains("=")) continue; // bare flag — ignored in this context

            String[] kv  = t.split("=", 2);
            String   key = kv[0].strip().toLowerCase();
            String   val = kv[1].strip();

            switch (key) {
                case "onfail"     -> onFail     = val.toLowerCase();
                case "timeout"    -> timeout    = parseIntSafe(val, -1);
                case "retry"      -> retry      = parseIntSafe(val, -1);
                case "masked"     -> masked     = Boolean.parseBoolean(val);
                case "screenshot" -> screenshot = Boolean.parseBoolean(val);
                case "delay"      -> delayMs    = parseIntSafe(val, 0);
                case "driver"     -> driver     = val.toLowerCase();
                default           -> { /* unknown option — silently ignored */ }
            }
        }

        return new StepOptions(onFail, timeout, retry, masked, screenshot, delayMs, driver);
    }

    /** Whether the onfail behaviour is {@code continue} (soft-assert mode). */
    public boolean isContinueOnFail() {
        return "continue".equalsIgnoreCase(onFail);
    }

    /** Whether the onfail behaviour is {@code warn}. */
    public boolean isWarnOnFail() {
        return "warn".equalsIgnoreCase(onFail);
    }

    /** Returns {@code true} if this step has a retry count embedded in {@code onfail}. */
    public boolean isRetryOnFail() {
        return onFail != null && onFail.toLowerCase().startsWith("retry:");
    }

    /** Extracts the retry count from {@code retry:N}; returns {@code 1} if unparseable. */
    public int retryCount() {
        if (!isRetryOnFail()) return 0;
        return parseIntSafe(onFail.substring(6), 1);
    }

    private static int parseIntSafe(String s, int fallback) {
        try {
            return Integer.parseInt(s.strip());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }
}
