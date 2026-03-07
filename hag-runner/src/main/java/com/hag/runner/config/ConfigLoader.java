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
            return new UrlConfig(appUrl, apiUrl);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load url.config.yml: " + e.getMessage(), e);
        }
    }

    // ── runner.config.yml ────────────────────────────────────────────────

    private static RunnerConfig loadRunnerConfig(Path root) {
        Path file = root.resolve("runner.config.yml");
        if (!Files.exists(file)) return new RunnerConfig(30, 1, "target/screenshots");

        try {
            JsonNode node        = YAML_MAPPER.readTree(file.toFile());
            JsonNode execution   = node.path("execution");
            JsonNode screenshots = node.path("screenshots");

            int    timeout = intVal(execution, "timeout-seconds", 30);
            int    retry   = intVal(execution, "retry-attempts",  1);
            String screenshotDir = text(screenshots, "directory", "target/screenshots");

            return new RunnerConfig(timeout, retry, screenshotDir);
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
                    "src/main/resources/scripts"
            );
        }

        try {
            JsonNode node  = YAML_MAPPER.readTree(file.toFile());
            JsonNode paths = node.path("paths");

            String testData  = text(paths, "test-data",  "src/main/resources/testdata");
            String templates = text(paths, "templates",  "src/main/resources/templates");
            String scripts   = text(paths, "scripts",    "src/main/resources/scripts");

            return new TestdataConfig(testData, templates, scripts);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load testdata.config.yml: " + e.getMessage(), e);
        }
    }

    /**
     * Also exposes database connection config for DbBootstrap wiring.
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

    /**
     * Resolves {@code ${env.VAR}} tokens from system environment variables.
     * Returns the raw string if no match found.
     */
    private static String resolveEnv(String value) {
        if (value == null || !value.startsWith("${env.")) return value;
        String varName = value.substring(6, value.length() - 1);
        String envVal  = System.getenv(varName);
        return envVal != null ? envVal : value;
    }

    // ── Inner config records ─────────────────────────────────────────────

    public record UrlConfig(String applicationUrl, String apiBaseUrl) {}
    public record RunnerConfig(int timeoutSeconds, int retryAttempts, String screenshotDir) {}
    public record TestdataConfig(String testDataPath, String templatesPath, String scriptsPath) {}
    public record DbConnectionConfig(String url, String username, String password) {}
}
