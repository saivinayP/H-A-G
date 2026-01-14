package hag.core.engine.dispatcher;

import hag.core.engine.context.ExecutionContext;
import hag.core.engine.executor.StepExecutor;
import hag.core.engine.model.Step;

public class ActionDispatcher {

    private final ActionRegistry registry;

    public ActionDispatcher(ActionRegistry registry) {
        this.registry = registry;
    }

    public void dispatch(Step step, ExecutionContext context) throws Exception {
        ActionCase parsed = ActionCase.parse(step.getAction());

        StepExecutor executor = registry
                .resolve(parsed.getAction(), parsed.getActionCase())
                .orElseThrow(() -> new IllegalStateException(
                        "No executor registered for action: " + parsed.getAction()
                ));

        executor.execute(step, context);
    }
}
