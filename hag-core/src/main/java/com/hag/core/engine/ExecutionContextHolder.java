package com.hag.core.engine;

import com.hag.core.context.ExecutionContext;

/**
 * Holds ExecutionContext per thread.
 * Enables safe parallel execution.
 */
public final class ExecutionContextHolder {

    private static final ThreadLocal<ExecutionContext> HOLDER =
            new ThreadLocal<>();

    private ExecutionContextHolder() {}

    public static void set(ExecutionContext context) {
        HOLDER.set(context);
    }

    public static ExecutionContext get() {
        return HOLDER.get();
    }

    public static void clear() {
        HOLDER.remove();
    }
}