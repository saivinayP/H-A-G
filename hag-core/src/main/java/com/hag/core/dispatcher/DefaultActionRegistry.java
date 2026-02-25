package com.hag.core.dispatcher;

import com.hag.core.executor.Action;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class DefaultActionRegistry
        implements ActionRegistry {

    private final Map<String, Action> actions =
            new ConcurrentHashMap<>();

    private volatile boolean frozen = false;

    @Override
    public void register(Action action) {

        if (frozen) {
            throw new IllegalStateException(
                    "Registry is frozen."
            );
        }

        actions.put(action.name().toLowerCase(), action);
    }

    @Override
    public Optional<Action> resolve(String actionName) {
        return Optional.ofNullable(
                actions.get(actionName.toLowerCase())
        );
    }

    @Override
    public void freeze() {
        this.frozen = true;
    }
}