package com.hag.core.engine.context;

import java.util.*;

public class DataStore {

    private final Map<ContextKey, Object> store = new HashMap<>();

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

    public Map<ContextKey, Object> snapshot() {
        return Collections.unmodifiableMap(store);
    }
}
