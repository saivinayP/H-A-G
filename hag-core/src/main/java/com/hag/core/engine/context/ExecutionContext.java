package com.hag.core.engine.context;

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
}
