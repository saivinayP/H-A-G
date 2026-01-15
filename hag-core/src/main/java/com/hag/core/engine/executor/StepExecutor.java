package com.hag.core.engine.executor;

import com.hag.core.engine.context.ExecutionContext;
import com.hag.core.engine.model.Step;
import com.hag.core.engine.result.ExecutionResult;

public interface StepExecutor {

    ExecutionResult execute(Step step, ExecutionContext context) throws Exception;
}
