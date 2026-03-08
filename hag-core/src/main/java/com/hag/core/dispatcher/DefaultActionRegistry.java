package com.hag.core.dispatcher;

import com.hag.core.executor.Action;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class DefaultActionRegistry
        implements ActionRegistry {

    private final Map<String, java.util.List<Action>> actions =
            new HashMap<>();

    private boolean frozen = false;

    @Override
    public void register(Action action) {

        if (frozen) {
            throw new IllegalStateException(
                    "Registry is frozen. No further registrations allowed."
            );
        }

        actions.computeIfAbsent(action.name().toUpperCase(), k -> new java.util.ArrayList<>())
               .add(action);
    }

    @Override
    public Optional<Action> resolve(String name) {
        java.util.List<Action> list = actions.get(name.toUpperCase());
        return (list == null || list.isEmpty()) ? Optional.empty() : Optional.of(list.get(0));
    }

    public java.util.List<Action> resolveAll(String name) {
        return actions.getOrDefault(name.toUpperCase(), java.util.Collections.emptyList());
    }

    @Override
    public void freeze() {
        frozen = true;
    }
}