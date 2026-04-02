package com.hag.db.action;

import com.hag.core.context.ExecutionContext;
import com.hag.core.dispatcher.descriptor.ActionDescriptor;
import com.hag.core.executor.Action;
import com.hag.core.executor.ActionCategory;
import com.hag.core.model.Step;
import com.hag.core.result.ExecutionResult;
import com.hag.db.model.DbQueryResult;

import java.math.BigDecimal;

/**
 * ASSERT_ROW_COUNT action — asserts the number of rows in the last DB_QUERY result.
 *
 * <h3>CSV Syntax</h3>
 * <pre>
 *   ASSERT_ROW_COUNT,,,1              → exactly 1 row
 *   ASSERT_ROW_COUNT:AT_LEAST,,,1    → 1 or more rows
 *   ASSERT_ROW_COUNT:AT_MOST,,,5     → 5 or fewer rows
 *   ASSERT_ROW_COUNT:ZERO,,,         → no rows (empty result)
 *   ASSERT_ROW_COUNT:NOT_ZERO,,,     → at least 1 row exists
 * </pre>
 */
public final class AssertRowCountAction implements Action {

    @Override
    public String name() { return "ASSERT_ROW_COUNT"; }

    @Override
    public ActionCategory category() { return ActionCategory.DB; }

    @Override
    public ExecutionResult execute(Step step, ActionDescriptor descriptor, ExecutionContext context) {

        DbQueryResult result = getLastResult(context);
        if (result == null) {
            return ExecutionResult.failure(
                    "ASSERT_ROW_COUNT — no DB query result found. Run DB_QUERY first."
            );
        }

        int actualCount = result.rowCount();
        String subCase  = descriptor.hasSubCase() ? descriptor.subCase() : "EQUALS";

        return switch (subCase) {
            case "ZERO"      -> actualCount == 0
                    ? ExecutionResult.success()
                    : ExecutionResult.failure("ASSERT_ROW_COUNT:ZERO failed — actual row count: " + actualCount);

            case "NOT_ZERO"  -> actualCount > 0
                    ? ExecutionResult.success()
                    : ExecutionResult.failure("ASSERT_ROW_COUNT:NOT_ZERO failed — result set is empty");

            case "AT_LEAST"  -> {
                int expected = parseExpected(step.getKey(), "ASSERT_ROW_COUNT:AT_LEAST");
                yield actualCount >= expected
                        ? ExecutionResult.success()
                        : ExecutionResult.failure(
                                "ASSERT_ROW_COUNT:AT_LEAST failed — expected >= " + expected
                                        + " but got " + actualCount);
            }
            case "AT_MOST"   -> {
                int expected = parseExpected(step.getKey(), "ASSERT_ROW_COUNT:AT_MOST");
                yield actualCount <= expected
                        ? ExecutionResult.success()
                        : ExecutionResult.failure(
                                "ASSERT_ROW_COUNT:AT_MOST failed — expected <= " + expected
                                        + " but got " + actualCount);
            }
            default          -> {   // EQUALS
                int expected = parseExpected(step.getKey(), "ASSERT_ROW_COUNT");
                yield actualCount == expected
                        ? ExecutionResult.success()
                        : ExecutionResult.failure(
                                "ASSERT_ROW_COUNT failed — expected: " + expected
                                        + " actual: " + actualCount);
            }
        };
    }

    private int parseExpected(String raw, String actionName) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException(actionName + " requires expected count in Key column");
        }
        try {
            return new BigDecimal(raw.trim()).intValueExact();
        } catch (Exception e) {
            throw new IllegalArgumentException(actionName + " — expected value is not a valid integer: " + raw);
        }
    }

    private DbQueryResult getLastResult(ExecutionContext context) {
        return context.getDataStore()
                .get(DbQueryAction.LAST_RESULT_KEY)
                .filter(v -> v instanceof DbQueryResult)
                .map(v -> (DbQueryResult) v)
                .orElse(null);
    }
}
