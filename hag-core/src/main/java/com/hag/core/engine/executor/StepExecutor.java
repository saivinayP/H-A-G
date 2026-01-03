package com.hag.core.engine.executor;

import com.hag.core.engine.context.ExecutionContext;
import com.hag.core.engine.model.Step;

public interface StepExecutor {

    void execute(Step step, ExecutionContext context) throws Exception;
}
