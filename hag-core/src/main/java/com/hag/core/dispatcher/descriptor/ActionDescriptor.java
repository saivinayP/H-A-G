package com.hag.core.dispatcher.descriptor;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * Immutable representation of a parsed action command.
 *
 * <p>An {@code ActionDescriptor} is produced by {@link ActionDescriptorParser#parse(String)}
 * from the Action CSV column value:
 *
 * <pre>
 *   CLICK                    → name=CLICK,  subCase=null
 *   CLICK:DOUBLE             → name=CLICK,  subCase=DOUBLE
 *   WAIT:TEXT                → name=WAIT,   subCase=TEXT
 *   INPUT|masked=true        → name=INPUT,  stepOptions={masked=true}
 *   ASSERT_STATUS|retry=5   → name=ASSERT_STATUS, stepOptions={retry=5}
 * </pre>
 *
 * <p>Modifier flags and key=value parameters — parsed from the Source column
 * via {@link ActionDescriptorParser#parseModifiers(String)} and held in a
 * {@link ModifierSet} — are attached to the descriptor after initial parsing
 * but are kept in a separate field on the Step for clarity.
 */
public final class ActionDescriptor {

    private final String              name;
    private final String              subCase;
    private final Set<String>         flags;
    private final Map<String, String> parameters;
    private final StepOptions         stepOptions;

    /** Full constructor — used by {@link ActionDescriptorParser#parse(String)}. */
    public ActionDescriptor(
            String              name,
            String              subCase,
            Set<String>         flags,
            Map<String, String> parameters,
            StepOptions         stepOptions
    ) {
        this.name        = name;
        this.subCase     = subCase;
        this.flags       = flags      != null ? flags      : Collections.emptySet();
        this.parameters  = parameters != null ? parameters : Collections.emptyMap();
        this.stepOptions = stepOptions != null ? stepOptions : StepOptions.defaults();
    }

    /**
     * Compatibility overload used by call sites that don't yet pass options.
     * Defaults to {@link StepOptions#defaults()}.
     */
    public ActionDescriptor(
            String              name,
            String              subCase,
            Set<String>         flags,
            Map<String, String> parameters
    ) {
        this(name, subCase, flags, parameters, StepOptions.defaults());
    }

    // ------------------------------------------------------------------ name

    /** The primary action name, always upper-case. Never {@code null}. */
    public String name() {
        return name;
    }

    // -------------------------------------------------------------- sub-case

    /**
     * The optional sub-case variant, always upper-case.
     * {@code null} when no colon was present in the Action column.
     *
     * <p>Example: for {@code CLICK:DOUBLE} this returns {@code "DOUBLE"}.
     */
    public String subCase() {
        return subCase;
    }

    /** @return {@code true} when a sub-case is present. */
    public boolean hasSubCase() {
        return subCase != null && !subCase.isBlank();
    }

    /**
     * Convenience method — returns {@code true} when
     * {@link #hasSubCase()} is true and the sub-case matches
     * {@code expected} (case-insensitive).
     */
    public boolean isSubCase(String expected) {
        return hasSubCase() && subCase.equalsIgnoreCase(expected);
    }

    // ------------------------------------------------------------------ flags

    /** @return {@code true} if the given flag (case-insensitive) is present. */
    public boolean hasFlag(String flag) {
        return flags.contains(flag.toLowerCase());
    }

    public Set<String> flags() {
        return Collections.unmodifiableSet(flags);
    }

    // --------------------------------------------------------------- params

    /**
     * Returns the value of the named parameter, or {@code null} if absent.
     * Parameter name lookup is case-insensitive.
     */
    public String getParameter(String key) {
        return parameters.get(key.toLowerCase());
    }

    public Map<String, String> parameters() {
        return Collections.unmodifiableMap(parameters);
    }

    // ---------------------------------------------------------- step options

    /**
     * Per-step execution options parsed from the Action column pipe-suffix.
     * Never {@code null} — returns {@link StepOptions#defaults()} when no
     * options were specified.
     */
    public StepOptions stepOptions() {
        return stepOptions;
    }

    @Override
    public String toString() {
        return "ActionDescriptor{name='" + name + "'"
                + (subCase != null ? ", subCase='" + subCase + "'" : "")
                + (flags.isEmpty()      ? "" : ", flags="      + flags)
                + (parameters.isEmpty() ? "" : ", parameters=" + parameters)
                + '}';
    }
}