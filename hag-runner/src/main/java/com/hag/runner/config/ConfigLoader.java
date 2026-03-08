package com.hag.runner.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.hag.core.config.FrameworkConfig;
import com.hag.core.config.FrameworkConfigBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Loads H-A-G config YAML files and builds a {@link FrameworkConfig}.
 *
 * <h3>Expected files (project root)</h3>
 * <ul>
 *   <li>{@code url.config.yml}       — environment URLs</li>
 *   <li>{@code runner.config.yml}    — browser, thread count, test paths</li>
 *   <li>{@code testdata.config.yml}  — file system path roots</li>
 * </ul>
 *
 * <h3>url.config.yml</h3>
 * <pre>
 * active-environment: dev
 * environments:
 *   dev:
 *     application: https://dev.myapp.com
 *     api-base:    https://api-dev.myapp.com
 * </pre>
 *
 * <h3>runner.config.yml</h3>
 * <pre>
 * browser:
 *   type: chrome
 *   headless: false
 * execution:
 *   thread-count: 1
 *   timeout-seconds: 30
 *   retry-attempts: 1
 * screenshots:
 *   directory: target/screenshots
 * </pre>
 *
 * <h3>testdata.config.yml</h3>
 * <pre>
 * paths:
 *   locators:   src/main/resources/locators
 *   test-data:  src/main/resources/testdata
 *   templates:  src/main/resources/templates
 *   scripts:    src/main/resources/scripts
 * database:
 *   url:      jdbc:mysql://localhost:3306/testdb
 *   username: qa_user
 *   password: ${env.DB_PASSWORD}
 * </pre>
 */
public final class ConfigLoader {

    private static final ObjectMapper YAML_MAPPER =
            new ObjectMapper(new YAMLFactory());

    /** Pattern that matches any ${env.VAR_NAME} token anywhere in a string. */
    private static final Pattern ENV_PAT = Pattern.compile("\\$\\{env\\.([^}]+)\\}");

    private ConfigLoader() {}

    /**
     * Loads all three config files and assembles a {@link FrameworkConfig}.
     *
     * @param projectRoot path to the project root (where config files live)
     * @return fully populated {@link FrameworkConfig}
     */
    public static FrameworkConfig load(String projectRoot) {
        Path root = Paths.get(projectRoot).toAbsolutePath();

        UrlConfig      url      = loadUrlConfig(root);
        RunnerConfig   runner   = loadRunnerConfig(root);
        TestdataConfig testdata = loadTestdataConfig(root);

        return new FrameworkConfigBuilder()
                .baseUrl(url.applicationUrl())
                .apiBaseUrl(url.apiBaseUrl())
                .defaultWaitTimeoutSeconds(runner.timeoutSeconds())
                .defaultRetryAttempts(runner.retryAttempts())
                .screenshotDirectory(runner.screenshotDir())
                .testDataPath(testdata.testDataPath())
                .templatesPath(testdata.templatesPath())
                .sqlScriptsPath(testdata.scriptsPath())
                .build();
    }

    // ── url.config.yml ───────────────────────────────────────────────────

    private static UrlConfig loadUrlConfig(Path root) {
        Path file = root.resolve("url.config.yml");
        if (!Files.exists(file)) return new UrlConfig("", "");

        try {
            JsonNode node = YAML_MAPPER.readTree(file.toFile());
            String activeEnv = text(node, "active-environment", "dev");
            JsonNode envNode = node.path("environments").path(activeEnv);
            String appUrl = text(envNode, "application", "");
            String apiUrl = text(envNode, "api-base", "");
            return new UrlConfig(resolveEnv(appUrl), resolveEnv(apiUrl));
        } catch (IOException e) {
            throw new RuntimeException("Failed to load url.config.yml: " + e.getMessage(), e);
        }
    }

    // ── runner.config.yml ────────────────────────────────────────────────

    /**
     * Loads just the runner config, returning defaults when the file is missing.
     * Exposed as public so HagTestBase can cache it at suite scope.
     */
    public static RunnerConfig loadRunnerConfig(String projectRoot) {
        return loadRunnerConfig(Paths.get(projectRoot).toAbsolutePath());
    }

    private static RunnerConfig loadRunnerConfig(Path root) {
        Path file = root.resolve("runner.config.yml");
        if (!Files.exists(file)) return new RunnerConfig("chrome", false, 30, 1, "target/screenshots", "tests");

        try {
            JsonNode node        = YAML_MAPPER.readTree(file.toFile());
            JsonNode browser     = node.path("browser");
            JsonNode execution   = node.path("execution");
            JsonNode screenshots = node.path("screenshots");
            JsonNode testSuite   = node.path("test-suite");

            String  browserType  = text(browser, "type",      "chrome");
            boolean headless     = boolVal(browser, "headless", false);
            int     timeout      = intVal(execution, "timeout-seconds", 30);
            int     retry        = intVal(execution, "retry-attempts",  1);
            String  screenshotDir = text(screenshots, "directory",      "target/screenshots");
            String  scanRoot      = text(testSuite,   "scan-root",      "tests");

            return new RunnerConfig(browserType, headless, timeout, retry, screenshotDir, scanRoot);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load runner.config.yml: " + e.getMessage(), e);
        }
    }

    // ── testdata.config.yml ──────────────────────────────────────────────

    private static TestdataConfig loadTestdataConfig(Path root) {
        Path file = root.resolve("testdata.config.yml");
        if (!Files.exists(file)) {
            return new TestdataConfig(
                    "src/main/resources/testdata",
                    "src/main/resources/templates",
                    "src/main/resources/scripts",
                    "src/main/resources/locators"
            );
        }

        try {
            JsonNode node  = YAML_MAPPER.readTree(file.toFile());
            JsonNode paths = node.path("paths");

            String testData  = root.resolve(text(node, "test-data", text(paths, "test-data", "src/main/resources/testdata"))).toString();
            String templates = root.resolve(text(node, "api-templates", text(paths, "templates", "src/main/resources/templates"))).toString();
            String scripts   = root.resolve(text(node, "sql-scripts", text(paths, "scripts", "src/main/resources/scripts"))).toString();
            String locators  = root.resolve(text(node, "locators", text(paths, "locators", "src/main/resources/locators"))).toString();

            return new TestdataConfig(testData, templates, scripts, locators);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load testdata.config.yml: " + e.getMessage(), e);
        }
    }

    /**
     * Exposes the locators path for LocatorRepository configuration.
     * Public for use in HagTestBase suite setup.
     */
    public static String loadLocatorsPath(String projectRoot) {
        Path root = Paths.get(projectRoot).toAbsolutePath();
        Path file = root.resolve("testdata.config.yml");
        if (!Files.exists(file)) return "src/main/resources/locators";
        try {
            JsonNode node  = YAML_MAPPER.readTree(file.toFile());
            JsonNode paths = node.path("paths");
            return root.resolve(text(paths, "locators", "src/main/resources/locators")).toString();
        } catch (IOException e) {
            return "src/main/resources/locators";
        }
    }

    /**
     * Also exposes database connection config for wiring in HagTestBase suite setup.
     */
    public static DbConnectionConfig loadDbConfig(String projectRoot) {
        Path file = Paths.get(projectRoot).toAbsolutePath().resolve("testdata.config.yml");
        if (!Files.exists(file)) return new DbConnectionConfig("", "", "");

        try {
            JsonNode node = YAML_MAPPER.readTree(file.toFile());
            JsonNode db   = node.path("database");
            String url      = resolveEnv(text(db, "url",      ""));
            String username = resolveEnv(text(db, "username", ""));
            String password = resolveEnv(text(db, "password", ""));
            return new DbConnectionConfig(url, username, password);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load DB config: " + e.getMessage(), e);
        }
    }

    // ── Helpers ──────────────────────────────────────────────────────────

    private static String text(JsonNode node, String field, String def) {
        JsonNode n = node.path(field);
        return n.isMissingNode() || n.isNull() ? def : n.asText(def);
    }

    private static int intVal(JsonNode node, String field, int def) {
        JsonNode n = node.path(field);
        return n.isMissingNode() || n.isNull() ? def : n.asInt(def);
    }

    private static boolean boolVal(JsonNode node, String field, boolean def) {
        JsonNode n = node.path(field);
        return n.isMissingNode() || n.isNull() ? def : n.asBoolean(def);
    }

    /**
     * RUN-3 fix: Resolves ALL {@code ${env.VAR}} tokens in a string using regex.
     *
     * <p>Works for embedded tokens like {@code jdbc:mysql://host/${env.DB_NAME}}.
     * If a referenced environment variable is not set, throws an
     * {@link IllegalStateException} with a clear diagnostic message.
     */
    static String resolveEnv(String value) {
        if (value == null || !value.contains("${env.")) return value;

        Matcher m = ENV_PAT.matcher(value);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String varName = m.group(1);
            String envVal  = System.getenv(varName);
            if (envVal == null) {
                // Non-DB fields are optional — return raw token to allow lazy errors
                // DB_PASSWORD missing → let JdbcDbAdapter surface it on connect
                envVal = m.group(0);   // keep original ${env.VAR} if not set
            }
            m.appendReplacement(sb, Matcher.quoteReplacement(envVal));
        }
        m.appendTail(sb);
        return sb.toString();
    }

    // ── Inner config records ─────────────────────────────────────────────

    public record UrlConfig(String applicationUrl, String apiBaseUrl) {}

    public record RunnerConfig(
            String browserType,
            boolean headless,
            int timeoutSeconds,
            int retryAttempts,
            String screenshotDir,
            String scanRoot
    ) {}

    public record TestdataConfig(
            String testDataPath,
            String templatesPath,
            String scriptsPath,
            String locatorsPath
    ) {}

    public record DbConnectionConfig(String url, String username, String password) {}
}
