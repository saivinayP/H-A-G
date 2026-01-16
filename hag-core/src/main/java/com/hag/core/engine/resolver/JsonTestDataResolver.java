package com.hag.core.engine.resolver;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.util.Map;

public class JsonTestDataResolver implements TestDataResolver {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @SuppressWarnings("unchecked")
    @Override
    public Object resolve(String dataFile, String key) {
        if (dataFile == null || key == null) {
            return null;
        }

        try {
            Map<String, Object> root = MAPPER.readValue(new File(dataFile), Map.class);
            String[] parts = key.split("\\.");
            Object current = root;

            for (String part : parts) {
                if (!(current instanceof Map)) {
                    throw new IllegalStateException(
                            "Invalid test data path: " + key
                    );
                }
                current = ((Map<String, Object>) current).get(part);
            }
            return current;
        } catch (Exception e) {
            throw new IllegalStateException(
                    "Failed to resolve test data from " + dataFile + " using key " + key, e
            );
        }
    }
}
