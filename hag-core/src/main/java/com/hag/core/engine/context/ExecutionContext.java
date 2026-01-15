package com.hag.core.engine.context;

import com.hag.core.engine.result.ExecutionResult;
import  java.util.Optional;

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
     *  - ${VAR}
     *  - ${SCOPE:VAR}
     *  - literal values
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
}
