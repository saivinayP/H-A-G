package hag.core.engine.executor.context;

import hag.core.engine.context.DataScope;
import hag.core.engine.context.ExecutionContext;
import hag.core.engine.executor.ContextStepExecutor;
import hag.core.engine.model.Step;

public class ChangeDataStoreExecutor implements ContextStepExecutor {

    @Override
    public void execute(Step step, ExecutionContext context) {
        ContextActionType action = ContextActionType.valueOf(step.getRecipient().toUpperCase());
        DataScope scope = DataScope.GLOBAL;
        String key = step.getSource();
        String value = step.getKey();

        switch(action) {
            case ADD, CHANGE -> context
                    .getDataStore()
                    .put(scope, key, value);

            case REMOVE -> context
                    .getDataStore()
                    .remove(scope, key);

            case CLEAR -> context
                    .getDataStore()
                    .clear();
        }
    }
}
