package hag.core.engine.executor;

import hag.core.engine.context.ExecutionContext;
import hag.core.engine.model.Step;

public interface StepExecutor {

    void execute(Step step, ExecutionContext context) throws Exception;
}
