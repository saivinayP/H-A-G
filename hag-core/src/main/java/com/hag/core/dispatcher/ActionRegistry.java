package com.hag.core.dispatcher;

import com.hag.core.executor.Action;

import java.util.Optional;

public interface ActionRegistry {

    void register(Action action);

    Optional<Action> resolve(String name);

    java.util.List<Action> resolveAll(String name);

    void freeze();
}