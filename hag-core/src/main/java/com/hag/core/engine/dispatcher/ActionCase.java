package com.hag.core.engine.dispatcher;

public final class ActionCase {

    private final String action;
    private final String actionCase;

    private ActionCase(String action, String actionCase) {
        this.action = action;
        this.actionCase = actionCase;
    }

    public static ActionCase parse(String rawAction) {
        if (rawAction == null || rawAction.isBlank()) {
            throw new IllegalArgumentException("Action must not be null");
        }

        int open = rawAction.indexOf('[');
        int close = rawAction.indexOf(']');

        if(open > 0 && close > open) {
            String action = rawAction.substring(0, open).trim();
            String actionCase = rawAction.substring(open + 1, close).trim();
            return new ActionCase(action, actionCase);
        }

        return new ActionCase(rawAction.trim(), null);
    }

    public String getAction() {
        return action;
    }
    public String getActionCase() {
        return actionCase;
    }
}
