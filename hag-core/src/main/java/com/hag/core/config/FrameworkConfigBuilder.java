package com.hag.core.config;

public final class FrameworkConfigBuilder {

    private int    defaultWaitTimeoutSeconds = 30;
    private int    defaultRetryAttempts      = 1;
    private String baseUrl                   = "";
    private String apiBaseUrl                = "";
    private String screenshotDirectory       = "target/screenshots";
    private String screenshotLevel           = "AT_FAILED_STEP";
    private String testDataPath              = "hag-resource/testdata";
    private String templatesPath             = "hag-resource/templates";
    private String sqlScriptsPath            = "hag-resource/scripts";

    public FrameworkConfigBuilder defaultWaitTimeoutSeconds(int v) { this.defaultWaitTimeoutSeconds = v; return this; }
    public FrameworkConfigBuilder defaultRetryAttempts(int v)      { this.defaultRetryAttempts      = v; return this; }
    public FrameworkConfigBuilder baseUrl(String v)                 { this.baseUrl                   = v; return this; }
    public FrameworkConfigBuilder apiBaseUrl(String v)              { this.apiBaseUrl                = v; return this; }
    public FrameworkConfigBuilder screenshotDirectory(String v)     { this.screenshotDirectory       = v; return this; }
    public FrameworkConfigBuilder screenshotLevel(String v)         { this.screenshotLevel           = v; return this; }
    public FrameworkConfigBuilder testDataPath(String v)            { this.testDataPath              = v; return this; }
    public FrameworkConfigBuilder templatesPath(String v)           { this.templatesPath             = v; return this; }
    public FrameworkConfigBuilder sqlScriptsPath(String v)          { this.sqlScriptsPath            = v; return this; }

    public FrameworkConfig build() {

        if (defaultWaitTimeoutSeconds <= 0) {
            throw new IllegalArgumentException("defaultWaitTimeoutSeconds must be positive");
        }
        if (defaultRetryAttempts <= 0) {
            throw new IllegalArgumentException("defaultRetryAttempts must be positive");
        }

        return new FrameworkConfig(
                defaultWaitTimeoutSeconds,
                defaultRetryAttempts,
                baseUrl,
                apiBaseUrl,
                screenshotDirectory,
                screenshotLevel,
                testDataPath,
                templatesPath,
                sqlScriptsPath
        );
    }
}