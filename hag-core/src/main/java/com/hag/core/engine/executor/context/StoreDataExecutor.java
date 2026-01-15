package com.hag.core.engine.executor.context;

import com.hag.core.engine.context.DataScope;
import com.hag.core.engine.context.ExecutionContext;
import com.hag.core.engine.executor.ContextStepExecutor;
import com.hag.core.engine.model.Step;
import com.hag.core.engine.result.EmptyExecutionResult;
import com.hag.core.engine.result.ExecutionResult;
import com.hag.core.engine.result.api.ApiExecutionResult;
import com.hag.core.engine.executor.context.api.ApiResultExtractor;

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

        String targetKey = step.getRecipient();
        String sourcePath = step.getSource();

        if (scope == DataScope.API) {
            ExecutionResult last = context.getLastResult();

            if (!(last instanceof ApiExecutionResult apiResult)) {
                throw new IllegalStateException(
                        "Previous step did not produce API result"
                );
            }

            Object extracted = ApiResultExtractor.extract(
                    apiResult.getResponseBody(),
                    sourcePath
            );

            context.getDataStore().put(scope, targetKey, extracted);
        } else {
            // existing GLOBAL / UI / DB handling (explicit values)
            context.getDataStore().put(scope, targetKey, sourcePath);
        }

        return EmptyExecutionResult.INSTANCE;
    }
}
