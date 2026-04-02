package com.hag.db.action;

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
 * DB_EXECUTE action — runs a SQL INSERT / UPDATE / DELETE statement.
 *
 * <h3>CSV Syntax</h3>
 * <pre>
 *   DB_EXECUTE,scripts/insert_order.sql,,
 *   DB_EXECUTE:INLINE,,,DELETE FROM sessions WHERE token = '${API:authToken}'
 * </pre>
 *
 * <ul>
 *   <li>{@code DB_EXECUTE}        — loads a {@code .sql} file from the scripts root</li>
 *   <li>{@code DB_EXECUTE:INLINE} — DML written directly in the Key column</li>
 * </ul>
 */
public final class DbExecuteAction implements Action {

    @Override
    public String name() { return "DB_EXECUTE"; }

    @Override
    public ActionCategory category() { return ActionCategory.DB; }

    @Override
    public ExecutionResult execute(Step step, ActionDescriptor descriptor, ExecutionContext context) {

        if (!context.hasDbAdapter()) {
            return ExecutionResult.failure("DB_EXECUTE requires a DbAdapter in ExecutionContext");
        }

        if (!(context.getDbAdapter() instanceof JdbcDbAdapter adapter)) {
            return ExecutionResult.failure("DB_EXECUTE requires JdbcDbAdapter");
        }

        try {
            Map<String, Object> vars = buildVarMap(context);
            String sql;

            if (descriptor.isSubCase("INLINE")) {
                String rawSql = step.getKey();
                if (rawSql == null || rawSql.isBlank()) {
                    return ExecutionResult.failure("DB_EXECUTE:INLINE requires SQL in Key column");
                }
                sql = SqlLoader.resolveInline(rawSql, vars);
            } else {
                String scriptPath = step.getRecipient();
                if (scriptPath == null || scriptPath.isBlank()) {
                    return ExecutionResult.failure("DB_EXECUTE requires SQL file path in Recipient column");
                }
                String sqlRoot = resolveSqlRoot(context);
                sql = SqlLoader.loadAndResolve(scriptPath, sqlRoot, vars);
            }

            int affected = adapter.executeUpdate(sql);
            return ExecutionResult.success("DB_EXECUTE → " + affected + " row(s) affected");

        } catch (Exception ex) {
            return ExecutionResult.failure("DB_EXECUTE failed: " + ex.getMessage());
        }
    }

    private Map<String, Object> buildVarMap(ExecutionContext context) {
        Map<String, Object> vars = new HashMap<>();
        context.getDataStore().snapshot().forEach(vars::put);
        return vars;
    }

    private String resolveSqlRoot(ExecutionContext context) {
        String path = context.getConfig() != null ? context.getConfig().getSqlScriptsPath() : null;
        return (path != null && !path.isBlank()) ? path : "hag-resource/scripts";
    }
}
