package com.hag.core.engine.executor.db;

import com.hag.core.engine.adapter.DbAdapter;
import com.hag.core.engine.context.ExecutionContext;
import com.hag.core.engine.executor.StepExecutor;
import com.hag.core.engine.model.Step;
import com.hag.core.engine.result.ExecutionResult;
import com.hag.core.engine.result.db.DbExecutionResult;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public class ExecuteQueryExecutor implements StepExecutor {

    @Override
    public ExecutionResult execute(
            Step step,
            ExecutionContext context
    ) {

        try {
            // 1. Load SQL
            String sql =
                    Files.readString(Path.of(step.getRecipient()));

            // 2. Execute via adapter
            DbAdapter adapter = context.getDbAdapter();

            List<Map<String, Object>> rows =
                    adapter.executeQuery(sql);

            // 3. Return result
            return new DbExecutionResult(rows);

        } catch (Exception e) {
            throw new IllegalStateException(
                    "DB execution failed for query: " +
                            step.getRecipient(),
                    e
            );
        }
    }
}
