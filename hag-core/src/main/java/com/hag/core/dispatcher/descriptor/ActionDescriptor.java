package com.hag.core.dispatcher.descriptor;

import java.util.Map;

public final class ActionDescriptor {

    private final String name;
    private final Map<String, String> variants;

    public ActionDescriptor(String name,
                            Map<String, String> variants) {
        this.name = name;
        this.variants = Map.copyOf(variants);
    }

    public String name() {
        return name;
    }

    public Map<String, String> variants() {
        return variants;
    }
}