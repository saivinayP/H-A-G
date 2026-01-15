package com.hag.core.engine.dispatcher;

import com.hag.core.engine.context.ExecutionContext;
import com.hag.core.engine.executor.StepExecutor;
import com.hag.core.engine.model.Step;
import com.hag.core.engine.result.ExecutionResult;

public class ActionDispatcher {

    private final ActionRegistry registry;

    public ActionDispatcher(ActionRegistry registry) {
        this.registry = registry;
    }

    public ExecutionResult dispatch(Step step, ExecutionContext context) throws Exception {
        ActionCase parsed = ActionCase.parse(step.getAction());

        StepExecutor executor = registry
                .resolve(parsed.getAction(), parsed.getActionCase())
                .orElseThrow(() -> new IllegalStateException(
                        "No executor registered for action: " + parsed.getAction()
                ));

        return executor.execute(step, context);
    }
}
