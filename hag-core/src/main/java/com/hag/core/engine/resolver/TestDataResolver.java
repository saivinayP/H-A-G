package com.hag.core.engine.resolver;

public interface TestDataResolver {

    /**
     * Resolves test data value.
     *
     * @param dataFile path to test data file (json)
     * @param key data block / path (e.g. positive.default)
     * @return resolved value or null
     */
    Object resolve(String dataFile, String key);
}
