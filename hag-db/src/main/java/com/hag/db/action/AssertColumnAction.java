package com.hag.db.action;

import com.hag.core.context.DataScope;
import com.hag.core.context.ExecutionContext;
import com.hag.core.dispatcher.descriptor.ActionDescriptor;
import com.hag.core.executor.Action;
import com.hag.core.executor.ActionCategory;
import com.hag.core.model.Step;
import com.hag.core.result.ExecutionResult;
import com.hag.db.model.DbQueryResult;

/**
 * ASSERT_COLUMN action — asserts a cell value in the last DB_QUERY result.
 *
 * <h3>CSV Syntax</h3>
 * <pre>
 *   ASSERT_COLUMN,email,,john@example.com          → first row, exact match
 *   ASSERT_COLUMN:CONTAINS,status,,ACTIVE          → first row, contains
 *   ASSERT_COLUMN:NOT_NULL,created_at,,            → first row, non-null
 *   ASSERT_COLUMN:NULL,deleted_at,,                → first row, is null
 *   ASSERT_COLUMN:ROW2,email,,jane@example.com     → row 2 (1-based), exact
 * </pre>
 *
 * <ul>
 *   <li><b>Recipient</b> — column name (case-insensitive)</li>
 *   <li><b>Key</b>       — expected value</li>
 *   <li><b>Source modifier</b> {@code row=N} — 1-based row index (default: 1)</li>
 * </ul>
 */
public final class AssertColumnAction implements Action {

    @Override
    public String name() { return "ASSERT_COLUMN"; }

    @Override
    public ActionCategory category() { return ActionCategory.DB; }

    @Override
    public ExecutionResult execute(Step step, ActionDescriptor descriptor, ExecutionContext context) {

        DbQueryResult result = getLastResult(context);
        if (result == null) {
            return ExecutionResult.failure("ASSERT_COLUMN — no DB result. Run DB_QUERY first.");
        }

        String column = step.getRecipient();
        if (column == null || column.isBlank()) {
            return ExecutionResult.failure("ASSERT_COLUMN requires column name in Recipient column");
        }

        // Row index — default 0 (first row), can be overridden by source modifier row=N (1-based)
        int rowIndex = 0;
        if (step.getModifiers() != null) {
            String rowParam = step.getModifiers().getParameter("row");
            if (rowParam != null) {
                try { rowIndex = Integer.parseInt(rowParam.trim()) - 1; }
                catch (NumberFormatException ignored) {}
            }
        }

        Object rawValue = result.getValue(rowIndex, column);
        String actual = rawValue == null ? null : rawValue.toString();

        String expected = resolveExpected(step, context);
        String subCase  = descriptor.hasSubCase() ? descriptor.subCase() : "EQUALS";

        return switch (subCase) {
            case "NOT_NULL"   -> actual != null
                    ? ExecutionResult.success()
                    : ExecutionResult.failure("ASSERT_COLUMN:NOT_NULL — [" + column + "] is null");

            case "NULL"       -> actual == null
                    ? ExecutionResult.success()
                    : ExecutionResult.failure("ASSERT_COLUMN:NULL — [" + column + "] has value: " + actual);

            case "CONTAINS"   -> (actual != null && expected != null && actual.contains(expected))
                    ? ExecutionResult.success()
                    : ExecutionResult.failure(
                            "ASSERT_COLUMN:CONTAINS [" + column + "] — actual: [" + actual
                                    + "] does not contain: [" + expected + "]");

            case "NOT_EQUALS" -> (actual != null && !actual.equals(expected))
                    ? ExecutionResult.success()
                    : ExecutionResult.failure(
                            "ASSERT_COLUMN:NOT_EQUALS [" + column + "] — value equals expected: " + expected);

            default           -> {   // EQUALS
                if (actual == null) {
                    yield ExecutionResult.failure("ASSERT_COLUMN [" + column + "] — column not found or value is null");
                }
                yield actual.equals(expected)
                        ? ExecutionResult.success()
                        : ExecutionResult.failure(
                                "ASSERT_COLUMN [" + column + "] failed — expected: ["
                                        + expected + "] actual: [" + actual + "]");
            }
        };
    }

    private String resolveExpected(Step step, ExecutionContext context) {
        String raw = step.getKey();
        if (raw == null) return null;
        Object resolved = context.resolveValue(raw);
        return resolved == null ? null : resolved.toString();
    }

    private DbQueryResult getLastResult(ExecutionContext context) {
        return context.getDataStore()
                .get(DataScope.GLOBAL, DbQueryAction.LAST_RESULT_KEY)
                .filter(v -> v instanceof DbQueryResult)
                .map(v -> (DbQueryResult) v)
                .orElse(null);
    }
}
