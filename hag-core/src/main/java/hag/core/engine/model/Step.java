package hag.core.engine.model;

import java.util.Objects;

public final class Step {

    private final String action;
    private final String recipient;
    private final String source;
    private final String key;
    private final String rawLine;

    public Step(
            String action,
            String recipient,
            String source,
            String key,
            String rawLine
    ) {
        this.action = Objects.requireNonNull(action, "action must not be null");
        this.recipient = recipient;
        this.source = source;
        this.key = key;
        this.rawLine = rawLine;
    }

    public String getAction() {
        return action;
    }

    public String getRecipient() {
        return recipient;
    }

    public String getSource() {
        return source;
    }

    public String getKey() {
        return key;
    }

    public String getRawLine() {
        return rawLine;
    }

    @Override
    public String toString() {
        return "Step{" +
                "action='" + action + '\'' +
                ", recipient='" + recipient + '\'' +
                ", source='" + source + '\'' +
                ", key='" + key + '\'' +
                '}';
    }
}
