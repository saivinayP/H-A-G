package com.hag.core.dispatcher;

import com.hag.core.executor.Action;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class DefaultActionRegistry
        implements ActionRegistry {

    private final Map<String, Action> actions =
            new HashMap<>();

    private boolean frozen = false;

    @Override
    public void register(Action action) {

        if (frozen) {
            throw new IllegalStateException(
                    "Registry is frozen. No further registrations allowed."
            );
        }

        actions.put(
                action.name().toUpperCase(),
                action
        );
    }

    @Override
    public Optional<Action> resolve(String name) {
        return Optional.ofNullable(
                actions.get(name.toUpperCase())
        );
    }

    @Override
    public void freeze() {
        frozen = true;
    }
}