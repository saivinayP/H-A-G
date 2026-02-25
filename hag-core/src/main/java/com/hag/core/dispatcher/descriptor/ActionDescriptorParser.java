package com.hag.core.dispatcher.descriptor;

import java.util.*;
import java.util.stream.Collectors;

public final class ActionDescriptorParser {

    public ActionDescriptor parse(String raw) {

        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("Action cannot be empty");
        }

        if (!raw.contains("[")) {
            return new ActionDescriptor(raw.trim(), Map.of());
        }

        String name = raw.substring(0, raw.indexOf("[")).trim();
        String content = raw.substring(
                raw.indexOf("[") + 1,
                raw.lastIndexOf("]")
        );

        Map<String, String> variants =
                Arrays.stream(content.split(","))
                        .map(String::trim)
                        .map(s -> s.split("="))
                        .collect(Collectors.toMap(
                                arr -> arr[0].trim(),
                                arr -> arr[1].trim()
                        ));

        return new ActionDescriptor(name, variants);
    }
}