package hag.core.engine.executor;

import hag.core.engine.context.ExecutionContext;
import hag.core.engine.model.Step;
import hag.core.engine.result.ExecutionResult;

public interface StepExecutor {

    ExecutionResult execute(Step step, ExecutionContext context) throws Exception;
}
