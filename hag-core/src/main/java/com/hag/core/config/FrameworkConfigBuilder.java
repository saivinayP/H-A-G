package com.hag.core.config;

public final class FrameworkConfigBuilder {

    private int    defaultWaitTimeoutSeconds = 30;
    private int    defaultRetryAttempts      = 1;
    private String baseUrl                   = "";
    private String apiBaseUrl                = "";
    private String screenshotDirectory       = "target/screenshots";
    private String testDataPath              = "src/main/resources/testdata";
    private String templatesPath             = "src/main/resources/templates";
    private String sqlScriptsPath            = "src/main/resources/scripts";

    public FrameworkConfigBuilder defaultWaitTimeoutSeconds(int v) { this.defaultWaitTimeoutSeconds = v; return this; }
    public FrameworkConfigBuilder defaultRetryAttempts(int v)      { this.defaultRetryAttempts      = v; return this; }
    public FrameworkConfigBuilder baseUrl(String v)                 { this.baseUrl                   = v; return this; }
    public FrameworkConfigBuilder apiBaseUrl(String v)              { this.apiBaseUrl                = v; return this; }
    public FrameworkConfigBuilder screenshotDirectory(String v)     { this.screenshotDirectory       = v; return this; }
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
                testDataPath,
                templatesPath,
                sqlScriptsPath
        );
    }
}