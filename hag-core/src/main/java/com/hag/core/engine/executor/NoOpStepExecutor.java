package com.hag.core.engine.executor;

import com.hag.core.engine.context.ExecutionContext;
import com.hag.core.engine.model.Step;

public class NoOpStepExecutor implements StepExecutor {

    @Override
    public void execute(Step step, ExecutionContext context) {
        // Intentionally does nothing
    }
}
