package com.hag.core.context;

import java.util.*;

/**
 * Flat variable store for a single test execution.
 *
 * <p>Uses {@link LinkedHashMap} to preserve insertion order for predictable
 * debug log output. {@link #snapshot()} returns a true copy so callers can
 * safely iterate it while the store is being mutated by concurrent actions.
 */
public class DataStore {

    private final Map<String, Object> store = new LinkedHashMap<>();

    public void put(String key, Object value) {
        store.put(key, value);
    }

    public Optional<Object> get(String key) {
        return Optional.ofNullable(store.get(key));
    }

    public boolean containsKey(String key) {
        return store.containsKey(key);
    }

    public void remove(String key) {
        store.remove(key);
    }

    public void clear() {
        store.clear();
    }

    /**
     * Returns an unmodifiable snapshot of the current store contents.
     */
    public Map<String, Object> snapshot() {
        return Collections.unmodifiableMap(new LinkedHashMap<>(store));
    }

    // ── Deprecated Scoped API (Mapped to Flat API) ───────────────────────────

    @Deprecated
    public void put(DataScope scope, String key, Object value) {
        put(key, value); // ignore scope
    }

    @Deprecated
    public Optional<Object> get(DataScope scope, String key) {
        return get(key); // ignore scope
    }

    @Deprecated
    public void remove(DataScope scope, String key) {
        remove(key); // ignore scope
    }
}
