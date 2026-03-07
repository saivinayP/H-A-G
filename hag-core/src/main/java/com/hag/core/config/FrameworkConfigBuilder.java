package com.hag.core.config;

public final class FrameworkConfigBuilder {

    private int defaultWaitTimeoutSeconds = 10;
    private int defaultRetryAttempts = 1;
    private String baseUrl = "";
    private String screenshotDirectory = "screenshots";

    public FrameworkConfigBuilder defaultWaitTimeoutSeconds(int value) {
        this.defaultWaitTimeoutSeconds = value;
        return this;
    }

    public FrameworkConfigBuilder defaultRetryAttempts(int value) {
        this.defaultRetryAttempts = value;
        return this;
    }

    public FrameworkConfigBuilder baseUrl(String value) {
        this.baseUrl = value;
        return this;
    }

    public FrameworkConfigBuilder screenshotDirectory(String value) {
        this.screenshotDirectory = value;
        return this;
    }

    public FrameworkConfig build() {

        if (defaultWaitTimeoutSeconds <= 0) {
            throw new IllegalArgumentException(
                    "defaultWaitTimeoutSeconds must be positive"
            );
        }

        if (defaultRetryAttempts <= 0) {
            throw new IllegalArgumentException(
                    "defaultRetryAttempts must be positive"
            );
        }

        return new FrameworkConfig(
                defaultWaitTimeoutSeconds,
                defaultRetryAttempts,
                baseUrl,
                screenshotDirectory
        );
    }
}