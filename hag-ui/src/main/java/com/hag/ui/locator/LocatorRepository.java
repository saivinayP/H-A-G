package com.hag.ui.locator;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Loads and caches locator JSON files.
 *
 * Thread-safe.
 */
public final class LocatorRepository {

    private static final ObjectMapper MAPPER =
            new ObjectMapper();

    private static final ConcurrentHashMap<String, Map<String, Object>> CACHE =
            new ConcurrentHashMap<>();

    private static final TypeReference<Map<String, Object>> MAP_TYPE =
            new TypeReference<>() {};

    private LocatorRepository() {}

    public static Map<String, Object> loadPage(String pageName) {

        validatePageName(pageName);

        return CACHE.computeIfAbsent(
                pageName,
                LocatorRepository::loadFromFile
        );
    }

    private static Map<String, Object> loadFromFile(String pageName) {

        String filePath = "locators/" + pageName + ".json";

        try (InputStream stream =
                     Thread.currentThread()
                             .getContextClassLoader()
                             .getResourceAsStream(filePath)) {

            if (stream == null) {
                throw new IllegalStateException(
                        "Locator file not found: " + filePath
                );
            }

            Map<String, Object> parsed =
                    MAPPER.readValue(stream, MAP_TYPE);

            validatePageStructure(parsed, pageName);

            return parsed;

        } catch (Exception ex) {

            throw new RuntimeException(
                    "Failed to load locator file: " + pageName,
                    ex
            );
        }
    }

    private static void validatePageName(String pageName) {

        if (pageName == null || pageName.isBlank()) {
            throw new IllegalArgumentException(
                    "Locator page name must not be null or blank"
            );
        }

        if (pageName.contains("..") || pageName.contains("/")) {
            throw new IllegalArgumentException(
                    "Invalid locator page name: " + pageName
            );
        }
    }

    private static void validatePageStructure(
            Map<String, Object> page,
            String pageName
    ) {

        if (page == null || page.isEmpty()) {
            throw new IllegalStateException(
                    "Locator file is empty: " + pageName
            );
        }

        for (Map.Entry<String, Object> entry : page.entrySet()) {

            if (!(entry.getValue() instanceof Map)) {
                throw new IllegalStateException(
                        "Invalid locator definition for key: "
                                + entry.getKey()
                                + " in page: "
                                + pageName
                );
            }
        }
    }
}