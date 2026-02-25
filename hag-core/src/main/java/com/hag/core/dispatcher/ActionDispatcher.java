package com.hag.core.dispatcher;

import com.hag.core.context.ExecutionContext;
import com.hag.core.model.Step;
import com.hag.core.result.ExecutionResult;

public interface ActionDispatcher {

    ExecutionResult dispatch(
            Step step,
            ExecutionContext context
    );
}