package com.hag.ui.action.wait;

public enum WaitCondition {

    PRESENCE,
    VISIBLE,
    CLICKABLE,
    INVISIBLE,
    TEXT_PRESENT;

    public static WaitCondition from(String value) {

        if (value == null || value.isBlank()) {
            return PRESENCE;
        }

        return WaitCondition.valueOf(
                value.trim().toUpperCase()
        );
    }
}