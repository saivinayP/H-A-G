package com.hag.core.config;

import java.util.Objects;

/**
 * Immutable framework-level configuration.
 */
public final class FrameworkConfig {

    private final int defaultWaitTimeoutSeconds;
    private final int defaultRetryAttempts;
    private final String baseUrl;
    private final String screenshotDirectory;

    FrameworkConfig(
            int defaultWaitTimeoutSeconds,
            int defaultRetryAttempts,
            String baseUrl,
            String screenshotDirectory
    ) {
        this.defaultWaitTimeoutSeconds = defaultWaitTimeoutSeconds;
        this.defaultRetryAttempts = defaultRetryAttempts;
        this.baseUrl = baseUrl;
        this.screenshotDirectory = screenshotDirectory;
    }

    public int getDefaultWaitTimeoutSeconds() {
        return defaultWaitTimeoutSeconds;
    }

    public int getDefaultRetryAttempts() {
        return defaultRetryAttempts;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public String getScreenshotDirectory() {
        return screenshotDirectory;
    }
}