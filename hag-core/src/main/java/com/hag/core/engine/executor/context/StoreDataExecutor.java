package com.hag.core.engine.executor.context;

import com.hag.core.engine.context.DataScope;
import com.hag.core.engine.context.ExecutionContext;
import com.hag.core.engine.executor.ContextStepExecutor;
import com.hag.core.engine.model.Step;
import com.hag.core.engine.result.EmptyExecutionResult;
import com.hag.core.engine.result.ExecutionResult;

public class StoreDataExecutor implements ContextStepExecutor {

    @Override
    public ExecutionResult execute(
            Step step,
            ExecutionContext context
    ) {

        DataScope scope = DataScope.valueOf(
                step.getAction()
                        .substring(
                                step.getAction().indexOf('[') + 1,
                                step.getAction().indexOf(']')
                        )
                        .toUpperCase()
        );

        String key = step.getRecipient();
        String value = step.getSource();

        context.getDataStore().put(scope, key, value);

        return EmptyExecutionResult.INSTANCE;
    }
}
