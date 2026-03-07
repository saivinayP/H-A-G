package com.hag.core.model;

import com.hag.core.dispatcher.descriptor.ModifierSet;

import java.util.Objects;

/**
 * Represents a single test step parsed from a CSV row.
 *
 * <h3>CSV columns</h3>
 * <pre>
 *   Action:Subcase  |  Recipient  |  Source  |  Key
 * </pre>
 *
 * <p>The {@code modifiers} field is populated by the execution engine after
 * parsing the Source column via {@link com.hag.core.dispatcher.descriptor.ActionDescriptorParser#parseModifiers(String)}.
 * It holds flags, parameters, and any file-path tokens found in the Source value.
 */
public final class Step {

    private final String action;
    private final String recipient;
    private final String source;
    private final String key;
    private final String rawLine;

    /**
     * Lazily-resolved modifier set derived from the Source column.
     * Populated at dispatch time; {@code null} before that.
     */
    private ModifierSet modifiers;

    public Step(
            String action,
            String recipient,
            String source,
            String key,
            String rawLine
    ) {
        this.action    = Objects.requireNonNull(action, "action must not be null");
        this.recipient = recipient;
        this.source    = source;
        this.key       = key;
        this.rawLine   = rawLine;
    }

    public String getAction()    { return action;    }
    public String getRecipient() { return recipient; }
    public String getSource()    { return source;    }
    public String getKey()       { return key;       }
    public String getRawLine()   { return rawLine;   }

    /**
     * Returns the parsed {@link ModifierSet} for this step's Source column.
     * May be {@code null} if the execution engine has not yet populated it.
     *
     * @see com.hag.core.dispatcher.descriptor.ActionDescriptorParser#parseModifiers(String)
     */
    public ModifierSet getModifiers() {
        return modifiers;
    }

    /**
     * Called by the execution engine to attach the resolved modifier set.
     */
    public void setModifiers(ModifierSet modifiers) {
        this.modifiers = modifiers;
    }

    @Override
    public String toString() {
        return "Step{"
                + "action='" + action + '\''
                + ", recipient='" + recipient + '\''
                + ", source='" + source + '\''
                + ", key='" + key + '\''
                + '}';
    }
}
