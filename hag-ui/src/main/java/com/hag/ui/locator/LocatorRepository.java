package com.hag.ui.locator;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Loads and caches locator JSON files.
 *
 * <h3>Resolution order</h3>
 * <ol>
 *   <li>Filesystem path: {@code <locatorRoot>/<PageName>.json}
 *       (configured via {@link #setLocatorRoot(Path)})</li>
 *   <li>Classpath fallback: {@code locators/<PageName>.json}</li>
 * </ol>
 *
 * <p>Thread-safe — uses {@link ConcurrentHashMap} for caching.
 */
public final class LocatorRepository {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final ConcurrentHashMap<String, Map<String, Object>> CACHE =
            new ConcurrentHashMap<>();

    private static final TypeReference<Map<String, Object>> MAP_TYPE =
            new TypeReference<>() {};

    /** Configured at boot time by FrameworkBootstrap / HagTestBase. */
    private static volatile Path locatorRoot;

    private LocatorRepository() {}

    /**
     * Sets the filesystem root for locator JSON files.
     * Called once during suite setup.
     */
    public static void setLocatorRoot(Path root) {
        locatorRoot = root;
        CACHE.clear();   // invalidate cache when root changes
    }

    public static Map<String, Object> loadPage(String pageName) {
        validatePageName(pageName);
        return CACHE.computeIfAbsent(pageName, LocatorRepository::loadFromSource);
    }

    /** Clears the in-memory cache (useful between test suites in the same JVM). */
    public static void clearCache() {
        CACHE.clear();
    }

    // ── Internal ─────────────────────────────────────────────────────────

    private static Map<String, Object> loadFromSource(String pageName) {

        // 1. Try filesystem path (configured locatorRoot takes priority)
        Path root = locatorRoot;
        if (root != null) {
            Path file = root.resolve(pageName + ".json");
            if (Files.exists(file)) {
                return loadFromFilesystem(file, pageName);
            }
        }

        // 2. Fallback: classpath (backward-compat for resources/locators/)
        return loadFromClasspath(pageName);
    }

    private static Map<String, Object> loadFromFilesystem(Path file, String pageName) {
        try {
            Map<String, Object> parsed = MAPPER.readValue(file.toFile(), MAP_TYPE);
            validatePageStructure(parsed, pageName);
            return parsed;
        } catch (IOException ex) {
            throw new RuntimeException("Failed to load locator file: " + file, ex);
        }
    }

    private static Map<String, Object> loadFromClasspath(String pageName) {
        String filePath = "locators/" + pageName + ".json";
        try (InputStream stream =
                     Thread.currentThread()
                             .getContextClassLoader()
                             .getResourceAsStream(filePath)) {

            if (stream == null) {
                throw new IllegalStateException(
                        "Locator file not found on filesystem or classpath: " + pageName
                                + ".json (locatorRoot=" + locatorRoot + ")"
                );
            }

            Map<String, Object> parsed = MAPPER.readValue(stream, MAP_TYPE);
            validatePageStructure(parsed, pageName);
            return parsed;

        } catch (IOException ex) {
            throw new RuntimeException("Failed to load locator file from classpath: " + pageName, ex);
        }
    }

    private static void validatePageName(String pageName) {
        if (pageName == null || pageName.isBlank()) {
            throw new IllegalArgumentException("Locator page name must not be null or blank");
        }
        if (pageName.contains("..") || pageName.contains("/") || pageName.contains("\\")) {
            throw new IllegalArgumentException("Invalid locator page name: " + pageName);
        }
    }

    private static void validatePageStructure(Map<String, Object> page, String pageName) {
        if (page == null || page.isEmpty()) {
            throw new IllegalStateException("Locator file is empty: " + pageName);
        }
        for (Map.Entry<String, Object> entry : page.entrySet()) {
            if (!(entry.getValue() instanceof Map)) {
                throw new IllegalStateException(
                        "Invalid locator definition for key: " + entry.getKey()
                                + " in page: " + pageName
                );
            }
        }
    }
}