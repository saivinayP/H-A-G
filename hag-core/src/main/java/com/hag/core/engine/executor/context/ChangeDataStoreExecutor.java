package com.hag.core.engine.executor.context;

import com.hag.core.engine.context.DataScope;
import com.hag.core.engine.context.ExecutionContext;
import com.hag.core.engine.executor.ContextStepExecutor;
import com.hag.core.engine.model.Step;

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
