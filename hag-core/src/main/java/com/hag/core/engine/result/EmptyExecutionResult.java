package com.hag.core.engine.result;

public final class EmptyExecutionResult extends ExecutionResult {
    public static final EmptyExecutionResult INSTANCE = new EmptyExecutionResult();

    private EmptyExecutionResult() {
        super(null);
    }
}
