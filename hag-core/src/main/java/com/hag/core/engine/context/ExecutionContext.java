package com.hag.core.engine.context;

import com.hag.core.engine.adapter.ApiAdapter;
import com.hag.core.engine.adapter.DbAdapter;
import com.hag.core.engine.adapter.UiAdapter;
import com.hag.core.engine.resolver.TestDataResolver;
import com.hag.core.engine.result.ExecutionResult;

import java.util.Optional;

public class ExecutionContext {

    private int currentStepIndex = 0;
    private final DataStore dataStore = new DataStore();

    // existing fields:
    // - DataStore
    // - lastResult
    // - step index, etc.

    /**
     * Resolves a value used in CSV.
     * Supports:
     * - ${VAR}
     * - ${SCOPE:VAR}
     * - literal values
     */
    public Object resolveValue(String input) {

        if (input == null) {
            return null;
        }

        String trimmed = input.trim();

        // Placeholder resolution
        if (trimmed.startsWith("${") && trimmed.endsWith("}")) {

            String token =
                    trimmed.substring(2, trimmed.length() - 1);

            DataScope scope = DataScope.GLOBAL;
            String key = token;

            if (token.contains(":")) {
                String[] parts = token.split(":", 2);
                scope = DataScope.valueOf(parts[0].toUpperCase());
                key = parts[1];
            }

            Optional<Object> value =
                    getDataStore().get(scope, key);

            return value.orElseThrow(() ->
                    new IllegalStateException(
                            "No value found for placeholder: " + trimmed
                    )
            );
        }

        // Literal value
        return trimmed;
    }

    public int nextStepIndex() {
        return ++currentStepIndex;
    }

    public int getCurrentStepIndex() {
        return currentStepIndex;
    }

    public DataStore getDataStore() {
        return dataStore;
    }

    public void reset() {
        currentStepIndex = 0;
        dataStore.clear();
    }

    private ExecutionResult lastResult;

    public void setLastResult(ExecutionResult lastResult) {
        this.lastResult = lastResult;
    }

    public ExecutionResult getLastResult() {
        return lastResult;
    }

    private UiAdapter uiAdapter;

    public UiAdapter getUiAdapter() {
        return uiAdapter;
    }

    public void setUiAdapter(UiAdapter uiAdapter) {
        this.uiAdapter = uiAdapter;
    }

    private TestDataResolver testDataResolver;

    public TestDataResolver getTestDataResolver() {
        return testDataResolver;
    }

    public void setTestDataResolver(TestDataResolver testDataResolver) {
        this.testDataResolver = testDataResolver;
    }

    private ApiAdapter apiAdapter;

    public ApiAdapter getApiAdapter() {
        return apiAdapter;
    }

    public void setApiAdapter(ApiAdapter apiAdapter) {
        this.apiAdapter = apiAdapter;
    }

    private DbAdapter dbAdapter;

    public DbAdapter getDbAdapter() {
        return dbAdapter;
    }

    public void setDbAdapter(DbAdapter dbAdapter) {
        this.dbAdapter = dbAdapter;
    }

    /**
     * Validates that all required runtime components
     * are configured before execution begins.
     *
     * This method must be called once at the start
     * of execution by the engine.
     */
    public void validateConfiguration() {

        if (uiAdapter == null) {
            throw new IllegalStateException("UiAdapter is not configured in ExecutionContext");
        }

        if (apiAdapter == null) {
            throw new IllegalStateException("ApiAdapter is not configured in ExecutionContext");
        }

        if (dbAdapter == null) {
            throw new IllegalStateException("DbAdapter is not configured in ExecutionContext");
        }

        if (testDataResolver == null) {
            throw new IllegalStateException("TestDataResolver is not configured in ExecutionContext");
        }
    }
}
