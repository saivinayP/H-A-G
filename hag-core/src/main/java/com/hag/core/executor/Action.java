package com.hag.core.executor;

import com.hag.core.context.ExecutionContext;
import com.hag.core.result.ExecutionResult;
import com.hag.core.model.Step;

public interface Action {
    String name();
    ExecutionResult execute(Step step, ExecutionContext context);
}