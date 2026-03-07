package com.hag.db.action;

import com.hag.core.context.DataScope;
import com.hag.core.context.ExecutionContext;
import com.hag.core.dispatcher.descriptor.ActionDescriptor;
import com.hag.core.executor.Action;
import com.hag.core.executor.ActionCategory;
import com.hag.core.model.Step;
import com.hag.core.result.ExecutionResult;
import com.hag.db.adapter.JdbcDbAdapter;
import com.hag.db.sql.SqlLoader;

import java.util.HashMap;
import java.util.Map;

/**
 * DB_QUERY action — runs a SQL SELECT and stores the result in the adapter.
 *
 * <h3>CSV Syntax</h3>
 * <pre>
 *   DB_QUERY,queries/get_user.sql,,
 *   DB_QUERY:INLINE,,,SELECT * FROM users WHERE id = '${UI:userId}'
 * </pre>
 *
 * <ul>
 *   <li>{@code DB_QUERY}        — loads a {@code .sql} file from the scripts root</li>
 *   <li>{@code DB_QUERY:INLINE} — SQL written directly in the Key column</li>
 * </ul>
 *
 * After execution, assertion and store actions read from {@link JdbcDbAdapter#getLastQueryResult()}.
 */
public final class DbQueryAction implements Action {

    /** Context key under which the last DbQueryResult is also stored in DataStore. */
    public static final String LAST_RESULT_KEY = "__db_last_result";

    @Override
    public String name() { return "DB_QUERY"; }

    @Override
    public ActionCategory category() { return ActionCategory.DB; }

    @Override
    public ExecutionResult execute(Step step, ActionDescriptor descriptor, ExecutionContext context) {

        if (!context.hasDbAdapter()) {
            return ExecutionResult.failure("DB_QUERY requires a DbAdapter in ExecutionContext");
        }

        if (!(context.getDbAdapter() instanceof JdbcDbAdapter adapter)) {
            return ExecutionResult.failure("DB_QUERY requires JdbcDbAdapter");
        }

        try {
            Map<String, Object> vars = buildVarMap(context);
            String sql;

            if (descriptor.isSubCase("INLINE")) {
                String rawSql = step.getKey();
                if (rawSql == null || rawSql.isBlank()) {
                    return ExecutionResult.failure("DB_QUERY:INLINE requires SQL in Key column");
                }
                sql = SqlLoader.resolveInline(rawSql, vars);
            } else {
                String scriptPath = step.getRecipient();
                if (scriptPath == null || scriptPath.isBlank()) {
                    return ExecutionResult.failure("DB_QUERY requires SQL file path in Recipient column");
                }
                String sqlRoot = resolveSqlRoot(context);
                sql = SqlLoader.loadAndResolve(scriptPath, sqlRoot, vars);
            }

            adapter.executeQuery(sql);   // result cached in adapter.getLastQueryResult()

            // Also store in DataStore for cross-action access
            context.getDataStore().put(DataScope.GLOBAL, LAST_RESULT_KEY, adapter.getLastQueryResult());

            int rowCount = adapter.getLastQueryResult() != null
                    ? adapter.getLastQueryResult().rowCount() : 0;

            return ExecutionResult.success("DB_QUERY → " + rowCount + " row(s) returned");

        } catch (Exception ex) {
            return ExecutionResult.failure("DB_QUERY failed: " + ex.getMessage());
        }
    }

    private Map<String, Object> buildVarMap(ExecutionContext context) {
        Map<String, Object> vars = new HashMap<>();
        context.getDataStore().snapshot().forEach((k, v) -> vars.put(k.getKey(), v));
        return vars;
    }

    private String resolveSqlRoot(ExecutionContext context) {
        String path = context.getConfig() != null ? context.getConfig().getSqlScriptsPath() : null;
        return (path != null && !path.isBlank()) ? path : "src/main/resources/scripts";
    }
}
