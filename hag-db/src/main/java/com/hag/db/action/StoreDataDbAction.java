package com.hag.db.action;

import com.hag.core.context.DataScope;
import com.hag.core.context.ExecutionContext;
import com.hag.core.dispatcher.descriptor.ActionDescriptor;
import com.hag.core.executor.Action;
import com.hag.core.executor.ActionCategory;
import com.hag.core.model.Step;
import com.hag.core.result.ExecutionResult;
import com.hag.core.db.DbQueryResult;

/**
 * STORE_DATA:DB — extracts a cell value from the last DB_QUERY result
 * and stores it in the DB DataStore scope.
 *
 * <h3>CSV Syntax</h3>
 * <pre>
 *   STORE_DATA:DB,email,,savedEmail        → stores first row email as 'savedEmail'
 *   STORE_DATA:DB_COUNT,,,totalOrders      → stores row count as 'totalOrders'
 * </pre>
 *
 * <ul>
 *   <li><b>Recipient</b> — column name to read (DB sub-case)</li>
 *   <li><b>Key</b>       — variable name to store into</li>
 *   <li><b>Source modifier</b> {@code row=N} — 1-based row index (default: 1)</li>
 * </ul>
 */
public final class StoreDataDbAction implements Action {

    @Override
    public String name() { return "STORE_DATA"; }

    @Override
    public ActionCategory category() { return ActionCategory.DB; }

    @Override
    public ExecutionResult execute(Step step, ActionDescriptor descriptor, ExecutionContext context) {

        if (!descriptor.hasSubCase()) {
            return ExecutionResult.skipped();
        }

        String subCase = descriptor.subCase();
        if (!subCase.equals("DB") && !subCase.equals("DB_COUNT")) {
            return ExecutionResult.skipped();
        }

        String variableName = step.getKey();
        if (variableName == null || variableName.isBlank()) {
            return ExecutionResult.failure("STORE_DATA:" + subCase + " requires variable name in Key column");
        }

        DbQueryResult result = getLastResult(context);
        if (result == null) {
            return ExecutionResult.failure("STORE_DATA:" + subCase + " — no DB result. Run DB_QUERY first.");
        }

        if (subCase.equals("DB_COUNT")) {
            String countStr = String.valueOf(result.rowCount());
            context.getDataStore().put(DataScope.DB, variableName, countStr);
            return ExecutionResult.success("Stored [" + variableName + "] = " + countStr);
        }

        // DB — extract column value
        String column = step.getRecipient();
        if (column == null || column.isBlank()) {
            return ExecutionResult.failure("STORE_DATA:DB requires column name in Recipient column");
        }

        int rowIndex = 0;
        if (step.getModifiers() != null) {
            String rowParam = step.getModifiers().getParameter("row");
            if (rowParam != null) {
                try { rowIndex = Integer.parseInt(rowParam.trim()) - 1; }
                catch (NumberFormatException ignored) {}
            }
        }

        Object rawValue = result.getValue(rowIndex, column);
        String value    = rawValue == null ? null : rawValue.toString();

        context.getDataStore().put(DataScope.DB, variableName, value);
        return ExecutionResult.success("Stored [" + variableName + "] = " + value);
    }

    private DbQueryResult getLastResult(ExecutionContext context) {
        return context.getDataStore()
                .get(DataScope.GLOBAL, DbQueryAction.LAST_RESULT_KEY)
                .filter(v -> v instanceof DbQueryResult)
                .map(v -> (DbQueryResult) v)
                .orElse(null);
    }
}
