package hag.core.engine.executor;

import hag.core.engine.context.ExecutionContext;
import hag.core.engine.model.Step;

public class NoOpStepExecutor implements StepExecutor {

    @Override
    public void execute(Step step, ExecutionContext context) {
        // Intentionally does nothing
    }
}
