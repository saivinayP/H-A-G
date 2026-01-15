package com.hag.core.engine.context;

import com.hag.core.engine.result.ExecutionResult;

public class ExecutionContext {

    private int currentStepIndex = 0;
    private final DataStore dataStore = new DataStore();

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
