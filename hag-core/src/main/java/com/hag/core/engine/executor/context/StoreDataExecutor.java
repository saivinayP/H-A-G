package com.hag.core.engine.executor.context;

import com.hag.core.engine.context.DataScope;
import com.hag.core.engine.context.ExecutionContext;
import com.hag.core.engine.executor.ContextStepExecutor;
import com.hag.core.engine.executor.context.api.ApiResultExtractor;
import com.hag.core.engine.model.Step;
import com.hag.core.engine.result.EmptyExecutionResult;
import com.hag.core.engine.result.ExecutionResult;
import com.hag.core.engine.result.api.ApiExecutionResult;
import com.hag.core.engine.result.db.DbExecutionResult;
import com.hag.core.engine.result.ui.UiExecutionResult;

public class StoreDataExecutor implements ContextStepExecutor {

    @Override
    public ExecutionResult execute(
            Step step,
            ExecutionContext context
    ) {

        DataScope scope = resolveScope(step.getAction());
        String targetKey = step.getRecipient();
        String sourcePath = step.getSource();

        if (targetKey == null || targetKey.isBlank()) {
            throw new IllegalArgumentException(
                    "StoreData requires a target variable name (Recipient column)"
            );
        }

        switch (scope) {

            case API -> storeFromApi(context, targetKey, sourcePath);

            case UI -> storeFromUi(context, targetKey, sourcePath);

            case DB -> storeFromDb(context, targetKey, sourcePath);

            case GLOBAL -> storeLiteral(context, targetKey, sourcePath);

            default -> throw new IllegalStateException(
                    "Unsupported StoreData scope: " + scope
            );
        }

        return EmptyExecutionResult.INSTANCE;
    }

    /* =========================
       Scope handlers
       ========================= */

    private void storeFromApi(
            ExecutionContext context,
            String targetKey,
            String sourcePath
    ) {
        ExecutionResult last = context.getLastResult();

        if (!(last instanceof ApiExecutionResult apiResult)) {
            throw new IllegalStateException(
                    "StoreData[API] requires a preceding API step"
            );
        }

        Object extracted = ApiResultExtractor.extract(
                apiResult.getResponseBody(),
                apiResult.getStatusCode(),
                apiResult.getHeaders(),
                sourcePath
        );

        context.getDataStore().put(DataScope.API, targetKey, extracted);
    }

    private void storeFromUi(
            ExecutionContext context,
            String targetKey,
            String sourcePath
    ) {
        ExecutionResult last = context.getLastResult();

        if (!(last instanceof UiExecutionResult uiResult)) {
            throw new IllegalStateException(
                    "StoreData[UI] requires a preceding UI step"
            );
        }

        Object extracted;

        if ("text".equalsIgnoreCase(sourcePath)) {
            extracted = uiResult.getText();
        } else if (sourcePath != null && sourcePath.startsWith("attr.")) {
            extracted = uiResult.getAttribute(
                    sourcePath.substring("attr.".length())
            );
        } else {
            throw new IllegalArgumentException(
                    "Invalid UI extraction path: " + sourcePath +
                            " (use 'text' or 'attr.<name>')"
            );
        }

        context.getDataStore().put(DataScope.UI, targetKey, extracted);
    }

    private void storeFromDb(
            ExecutionContext context,
            String targetKey,
            String sourcePath
    ) {
        ExecutionResult last = context.getLastResult();

        if (!(last instanceof DbExecutionResult dbResult)) {
            throw new IllegalStateException(
                    "StoreData[DB] requires a preceding DB step"
            );
        }

        Object extracted = ApiResultExtractor.extract(
                dbResult.getRows(),
                null,
                null,
                sourcePath
        );

        context.getDataStore().put(DataScope.DB, targetKey, extracted);
    }

    private void storeLiteral(
            ExecutionContext context,
            String targetKey,
            String value
    ) {
        context.getDataStore().put(DataScope.GLOBAL, targetKey, value);
    }

    /* =========================
       Helpers
       ========================= */

    private DataScope resolveScope(String action) {
        if (!action.contains("[") || !action.contains("]")) {
            return DataScope.GLOBAL;
        }

        String scope =
                action.substring(
                        action.indexOf('[') + 1,
                        action.indexOf(']')
                );

        return DataScope.valueOf(scope.toUpperCase());
    }
}
