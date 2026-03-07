package com.hag.core.config;

/**
 * Immutable framework-level configuration.
 *
 * <p>Built via {@link FrameworkConfigBuilder}. Holds all paths and timeouts
 * needed by the core engine plus the UI, API, and DB adapters.
 */
public final class FrameworkConfig {

    private final int    defaultWaitTimeoutSeconds;
    private final int    defaultRetryAttempts;

    // ── URL / path config ────────────────────────────────────────────────
    private final String baseUrl;             // Browser base URL
    private final String apiBaseUrl;          // REST/SOAP API base URL
    private final String screenshotDirectory;
    private final String testDataPath;        // root for test-data JSON files
    private final String templatesPath;       // root for API request templates
    private final String sqlScriptsPath;      // root for .sql files

    FrameworkConfig(
            int    defaultWaitTimeoutSeconds,
            int    defaultRetryAttempts,
            String baseUrl,
            String apiBaseUrl,
            String screenshotDirectory,
            String testDataPath,
            String templatesPath,
            String sqlScriptsPath
    ) {
        this.defaultWaitTimeoutSeconds = defaultWaitTimeoutSeconds;
        this.defaultRetryAttempts      = defaultRetryAttempts;
        this.baseUrl                   = baseUrl;
        this.apiBaseUrl                = apiBaseUrl;
        this.screenshotDirectory       = screenshotDirectory;
        this.testDataPath              = testDataPath;
        this.templatesPath             = templatesPath;
        this.sqlScriptsPath            = sqlScriptsPath;
    }

    public int    getDefaultWaitTimeoutSeconds() { return defaultWaitTimeoutSeconds; }
    public int    getDefaultRetryAttempts()      { return defaultRetryAttempts;      }
    public String getBaseUrl()                   { return baseUrl;                   }
    public String getApiBaseUrl()                { return apiBaseUrl;                }
    public String getScreenshotDirectory()       { return screenshotDirectory;       }
    public String getTestDataPath()              { return testDataPath;              }
    public String getTemplatesPath()             { return templatesPath;             }
    public String getSqlScriptsPath()            { return sqlScriptsPath;            }
}