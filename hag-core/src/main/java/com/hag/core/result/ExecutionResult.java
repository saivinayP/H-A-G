package com.hag.core.result;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * Immutable result of a single action execution.
 *
 * <h3>Status semantics</h3>
 * <ul>
 *   <li>{@link Status#SUCCESS} — action completed successfully</li>
 *   <li>{@link Status#FAILURE} — action failed; {@link #getMessage()} carries the reason</li>
 *   <li>{@link Status#SKIPPED} — action intentionally did not handle this sub-case;
 *       the dispatcher may try another registered handler with the same primary name</li>
 * </ul>
 */
public final class ExecutionResult {

    public enum Status { SUCCESS, FAILURE, SKIPPED }

    private final Status status;
    private final String message;
    private final Map<String, Object> data;

    private ExecutionResult(
            Status status,
            String message,
            Map<String, Object> data
    ) {
        this.status  = Objects.requireNonNull(status);
        this.message = message;
        this.data = data == null
                ? Collections.emptyMap()
                : Collections.unmodifiableMap(data);
    }

    // ── Factory methods ───────────────────────────────────────────────────

    public static ExecutionResult success() {
        return new ExecutionResult(Status.SUCCESS, null, null);
    }

    public static ExecutionResult success(String message) {
        return new ExecutionResult(Status.SUCCESS, message, null);
    }

    public static ExecutionResult success(Map<String, Object> data) {
        return new ExecutionResult(Status.SUCCESS, null, data);
    }

    /**
     * Signals that this action intentionally did not handle the sub-case.
     * The dispatcher will try another registered handler.
     */
    public static ExecutionResult skipped() {
        return new ExecutionResult(Status.SKIPPED, "SKIPPED", null);
    }

    public static ExecutionResult failure(String message) {
        return new ExecutionResult(
                Status.FAILURE,
                Objects.requireNonNullElse(message, "Execution failed"),
                null
        );
    }

    public static ExecutionResult failure(String message, Map<String, Object> data) {
        return new ExecutionResult(
                Status.FAILURE,
                Objects.requireNonNullElse(message, "Execution failed"),
                data
        );
    }

    // ── Query methods ─────────────────────────────────────────────────────

    public Status  getStatus()    { return status;  }
    public String  getMessage()   { return message; }
    public Map<String, Object> getData() { return data; }

    public boolean isSuccess() { return status == Status.SUCCESS;  }
    public boolean isFailure() { return status == Status.FAILURE;  }
    public boolean isSkipped() { return status == Status.SKIPPED;  }

    public boolean hasData() { return !data.isEmpty(); }
}