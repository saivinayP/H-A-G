package com.hag.core.resolver;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Loads and caches test data JSON files.
 *
 * Thread-safe.
 */
public class DefaultTestDataResolver
        implements TestDataResolver {

    private static final ObjectMapper MAPPER =
            new ObjectMapper();

    private static final ConcurrentHashMap<String, Map<String, Object>> CACHE =
            new ConcurrentHashMap<>();

    @Override
    @SuppressWarnings("unchecked")
    public Object resolve(
            String sourceExpression,
            String key
    ) {

        if (sourceExpression == null
                || sourceExpression.isBlank()) {

            throw new IllegalArgumentException(
                    "Test data source cannot be null or blank"
            );
        }

        if (key == null
                || key.isBlank()) {

            throw new IllegalArgumentException(
                    "Test data key cannot be null or blank"
            );
        }

        String[] parts =
                sourceExpression.split("\\.");

        if (parts.length < 3) {
            throw new IllegalArgumentException(
                    "Invalid test data reference: "
                            + sourceExpression
            );
        }

        String folder = parts[1];
        String file = parts[2];

        String fileIdentifier =
                folder + "/" + file;

        Map<String, Object> json =
                CACHE.computeIfAbsent(
                        fileIdentifier,
                        id -> loadFromFile(folder, file)
                );

        Object value = json.get(key);

        if (value == null) {
            throw new IllegalStateException(
                    "Test data key not found: " + key
            );
        }

        return value;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> loadFromFile(
            String folder,
            String file
    ) {

        try {

            String filePath =
                    "testdata/" + folder + "/" + file + ".json";

            InputStream stream =
                    Thread.currentThread()
                            .getContextClassLoader()
                            .getResourceAsStream(filePath);

            if (stream == null) {
                throw new IllegalStateException(
                        "Test data file not found: "
                                + filePath
                );
            }

            return MAPPER.readValue(stream, Map.class);

        } catch (Exception ex) {
            throw new RuntimeException(
                    "Failed to load test data file: "
                            + folder + "/" + file,
                    ex
            );
        }
    }
}