package com.hag.core.executor;

import com.hag.core.context.ExecutionContext;
import com.hag.core.model.Step;
import com.hag.core.result.ExecutionResult;

public interface Action {

    String name();

    ActionCategory category();

    ExecutionResult execute(
            Step step,
            ExecutionContext context
    );
}