package com.hag.core.engine.dispatcher;

import java.util.Objects;

public final class ActionKey {

    private final String action;
    private final String actionCase;

    public ActionKey(String action, String actionCase) {
        this.action = Objects.requireNonNull(action);
        this.actionCase = actionCase;
    }

    public static ActionKey of(String action, String actionCase) {
        return new ActionKey(action.toLowerCase(), actionCase == null ? null : actionCase.toLowerCase());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ActionKey that)) return false;
        return action.equals(that.action) &&
                Objects.equals(actionCase, that.actionCase);
    }

    @Override
    public int hashCode() {
        return Objects.hash(action, actionCase);
    }

    @Override
    public String toString() {
        return actionCase == null ? action : action + "[" + actionCase + "]";
    }
}
