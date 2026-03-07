package com.hag.core.dispatcher.descriptor;

import java.util.*;

/**
 * Holds the parsed result of a Source-column modifier string.
 *
 * <p>Source column format (pipe-separated):
 * <pre>
 *   clear|trim|scope=GLOBAL|testdata/login/users.json
 *   ─────┬───┬───────────────────────────────────────
 *   flag  flag  param                  file path
 * </pre>
 */
public final class ModifierSet {

    private static final ModifierSet EMPTY =
            new ModifierSet(
                    Collections.emptySet(),
                    Collections.emptyMap(),
                    Collections.emptyList()
            );

    private final Set<String>         flags;
    private final Map<String, String> parameters;
    private final List<String>        filePaths;

    ModifierSet(
            Set<String> flags,
            Map<String, String> parameters,
            List<String> filePaths
    ) {
        this.flags      = Collections.unmodifiableSet(flags);
        this.parameters = Collections.unmodifiableMap(parameters);
        this.filePaths  = Collections.unmodifiableList(filePaths);
    }

    public static ModifierSet empty() {
        return EMPTY;
    }

    /** Returns {@code true} if the given flag (case-insensitive) is present. */
    public boolean hasFlag(String flag) {
        return flags.contains(flag.toLowerCase());
    }

    /**
     * Returns the value of the named parameter, or {@code null} if absent.
     * Parameter name is case-insensitive.
     */
    public String getParameter(String name) {
        return parameters.get(name.toLowerCase());
    }

    /**
     * Returns {@code true} if there is at least one file path
     * (i.e. the Source column refers to a test data file).
     */
    public boolean hasFilePath() {
        return !filePaths.isEmpty();
    }

    /**
     * Returns the first file path token, or {@code null} if none.
     * Typically the Source column contains at most one file path.
     */
    public String getFilePath() {
        return filePaths.isEmpty() ? null : filePaths.get(0);
    }

    public Set<String>         getFlags()      { return flags; }
    public Map<String, String> getParameters() { return parameters; }
    public List<String>        getFilePaths()  { return filePaths; }

    @Override
    public String toString() {
        return "ModifierSet{flags=" + flags
                + ", parameters=" + parameters
                + ", filePaths=" + filePaths + '}';
    }
}
