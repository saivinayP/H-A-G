package com.hag.core.context;

import java.util.*;

/**
 * Scoped variable store for a single test execution.
 *
 * <p>Uses {@link LinkedHashMap} to preserve insertion order for predictable
 * debug log output. {@link #snapshot()} returns a true copy so callers can
 * safely iterate it while the store is being mutated by concurrent actions
 * (API STORE_DATA + UI STORE_DATA in the same step chain).
 */
public class DataStore {

    private final Map<ContextKey, Object> store = new LinkedHashMap<>();

    public void put(DataScope scope, String key, Object value) {
        store.put(new ContextKey(scope, key), value);
    }

    public Optional<Object> get(DataScope scope, String key) {
        return Optional.ofNullable(store.get(new ContextKey(scope, key)));
    }

    public void remove(DataScope scope, String key) {
        store.remove(new ContextKey(scope, key));
    }

    public void clear() {
        store.clear();
    }

    /**
     * Returns an unmodifiable snapshot of the current store contents.
     *
     * <p>This is a <em>copy</em> of the live map — safe to iterate even while
     * other threads write to the store (each thread has its own
     * {@code ExecutionContext}, so cross-thread mutation shouldn't happen, but
     * within a single test the snapshot may be iterated while FINALLY steps run).
     */
    public Map<ContextKey, Object> snapshot() {
        return Collections.unmodifiableMap(new LinkedHashMap<>(store));
    }
}
