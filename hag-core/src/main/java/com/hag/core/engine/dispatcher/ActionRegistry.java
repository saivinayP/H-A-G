package com.hag.core.engine.dispatcher;

import com.hag.core.engine.executor.StepExecutor;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ActionRegistry {

    private final Map<ActionKey, StepExecutor> registry = new HashMap<>();

    public void register(String action, String actionCase, StepExecutor executor) {
        ActionKey key = ActionKey.of(action, actionCase);
        registry.put(key, executor);
    }

    public Optional<StepExecutor> resolve(String action, String actionCase) {
        return Optional.ofNullable(registry.get(ActionKey.of(action, actionCase)));
    }
}
