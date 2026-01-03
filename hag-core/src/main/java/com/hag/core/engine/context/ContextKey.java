package com.hag.core.engine.context;

import java.util.Objects;

public final class ContextKey {
    private final DataScope scope;
    private final String key;

    public ContextKey(DataScope scope, String key) {
        this.scope = Objects.requireNonNull(scope, "scope can't be null");
        this.key = Objects.requireNonNull(key, "key can't be null");
    }

    public DataScope getScope() {
        return scope;
    }

    public String getKey() {
        return key;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ContextKey that)) return false;
        return scope == that.scope && key.equals(that.key);
    }

    @Override
    public int hashCode() {
        return Objects.hash(scope, key);
    }

    @Override
    public String toString() {
        return scope + ":" + key;
    }
}
