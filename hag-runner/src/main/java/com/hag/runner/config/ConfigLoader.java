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
 * Loads H-A-G config from the unified hag.yml file.
 */
public final class ConfigLoader {

    private static final ObjectMapper YAML_MAPPER = new ObjectMapper(new YAMLFactory());
    private static final Pattern ENV_PAT = Pattern.compile("\\$\\{env\\.([^}]+)\\}");
    private static final Pattern NEW_ENV_PAT = Pattern.compile("\\$\\{([A-ZA-z0-9_]+)(:([^}]+))?\\}");

    private ConfigLoader() {}

    private static JsonNode loadRootNode(Path root) {
        Path file = root.resolve("hag.yml");
        if (!Files.exists(file)) {
            throw new RuntimeException("Unified hag.yml not found at " + file.toAbsolutePath());
        }
        try {
            return YAML_MAPPER.readTree(file.toFile());
        } catch (IOException e) {
            throw new RuntimeException("Failed to read hag.yml: " + e.getMessage(), e);
        }
    }

    public static FrameworkConfig load(String projectRoot) {
        Path root = Paths.get(projectRoot).toAbsolutePath();
        JsonNode node = loadRootNode(root);

        String activeProfile = resolveEnv(text(node, "profile", "local"));
        JsonNode envNode = node.path("environments").path(activeProfile);

        String appUrl = text(envNode.path("url"), "application", "");
        String apiUrl = text(envNode.path("url"), "api-base", "");

        RunnerConfig runner = parseRunner(node);
        TestdataConfig testdata = parseTestdata(node, root);

        return new FrameworkConfigBuilder()
                .baseUrl(resolveEnv(appUrl))
                .apiBaseUrl(resolveEnv(apiUrl))
                .defaultWaitTimeoutSeconds(runner.timeoutSeconds())
                .defaultRetryAttempts(runner.retryAttempts())
                .screenshotDirectory(runner.screenshotDir())
                .screenshotLevel(runner.screenshotLevel())
                .testDataPath(testdata.testDataPath())
                .templatesPath(testdata.templatesPath())
                .sqlScriptsPath(testdata.scriptsPath())
                .build();
    }

    public static RunnerConfig loadRunnerConfig(String projectRoot) {
        return parseRunner(loadRootNode(Paths.get(projectRoot).toAbsolutePath()));
    }

    public static ReportingConfig loadReportingConfig(String projectRoot) {
        return parseReporting(loadRootNode(Paths.get(projectRoot).toAbsolutePath()));
    }

    public static DbConnectionConfig loadDbConfig(String projectRoot) {
        JsonNode node = loadRootNode(Paths.get(projectRoot).toAbsolutePath());
        String activeProfile = resolveEnv(text(node, "profile", "local"));
        JsonNode dbNode = node.path("environments").path(activeProfile).path("database");
        
        String url = resolveEnv(text(dbNode, "url", ""));
        String username = resolveEnv(text(dbNode, "username", ""));
        String password = resolveEnv(text(dbNode, "password", ""));
        return new DbConnectionConfig(url, username, password);
    }

    public static String loadLocatorsPath(String projectRoot) {
        Path root = Paths.get(projectRoot).toAbsolutePath();
        TestdataConfig cfg = parseTestdata(loadRootNode(root), root);
        return cfg.locatorsPath();
    }

    private static RunnerConfig parseRunner(JsonNode node) {
        JsonNode browser = node.path("browser");
        JsonNode execution = node.path("execution");
        JsonNode screenshots = node.path("screenshots");
        JsonNode paths = node.path("paths");

        return new RunnerConfig(
                text(browser, "type", "chrome"),
                boolVal(browser, "headless", false),
                text(execution, "mode", "local"),
                text(execution, "grid-url", ""),
                intVal(execution, "timeout-seconds", 30),
                intVal(execution, "retry-attempts", 1),
                text(screenshots, "directory", "target/screenshots"),
                text(screenshots, "level", "AT_FAILED_STEP"),
                text(paths, "test-suite", "hag-resource/tests")
        );
    }

    private static TestdataConfig parseTestdata(JsonNode node, Path root) {
        JsonNode paths = node.path("paths");
        String testData = root.resolve(text(paths, "test-data", "hag-resource/testdata")).toString();
        String templates = root.resolve(text(paths, "templates", "hag-resource/templates")).toString();
        String scripts = root.resolve(text(paths, "scripts", "hag-resource/scripts")).toString();
        String locators = root.resolve(text(paths, "locators", "hag-resource/locators")).toString();
        return new TestdataConfig(testData, templates, scripts, locators);
    }

    private static ReportingConfig parseReporting(JsonNode node) {
        JsonNode reporting = node.path("reporting");
        JsonNode jsonNode = reporting.path("json");
        JsonNode rpNode = reporting.path("report-portal");

        return new ReportingConfig(
                new ReportingConfig.JsonConfig(
                        boolVal(jsonNode, "enabled", false),
                        text(jsonNode, "output-dir", "TEST_RESULTS/json")
                ),
                new ReportingConfig.ReportPortalConfig(
                        boolVal(rpNode, "enabled", false),
                        text(rpNode, "endpoint", ""),
                        resolveEnv(text(rpNode, "api-token", "")),
                        text(rpNode, "project", "hag-project"),
                        text(rpNode, "launch-name", "")
                )
        );
    }

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

    static String resolveEnv(String value) {
        if (value == null) return null;
        
        // Legacy ${env.XYZ} support
        if (value.contains("${env.")) {
            Matcher m = ENV_PAT.matcher(value);
            StringBuffer sb = new StringBuffer();
            while (m.find()) {
                String varName = m.group(1);
                String envVal = System.getenv(varName);
                if (envVal == null) envVal = m.group(0);
                m.appendReplacement(sb, Matcher.quoteReplacement(envVal));
            }
            m.appendTail(sb);
            value = sb.toString();
        }

        // Standard ${VAR:default} support
        if (value.contains("${")) {
            Matcher m = NEW_ENV_PAT.matcher(value);
            StringBuffer sb = new StringBuffer();
            while (m.find()) {
                String varName = m.group(1);
                String defaultVal = m.group(3) != null ? m.group(3) : "";
                String envVal = System.getenv(varName);
                if (envVal == null || envVal.isBlank()) envVal = defaultVal;
                // if it's completely missing and no default, keep token to fail at connection phase if needed
                if (envVal.isBlank() && m.group(3) == null) envVal = m.group(0);
                m.appendReplacement(sb, Matcher.quoteReplacement(envVal));
            }
            m.appendTail(sb);
            value = sb.toString();
        }

        return value;
    }

    public record RunnerConfig(
            String browserType,
            boolean headless,
            String executionMode,
            String gridUrl,
            int timeoutSeconds,
            int retryAttempts,
            String screenshotDir,
            String screenshotLevel,
            String scanRoot
    ) {}

    public record TestdataConfig(
            String testDataPath,
            String templatesPath,
            String scriptsPath,
            String locatorsPath
    ) {}

    public record DbConnectionConfig(String url, String username, String password) {}

    public record ReportingConfig(
            JsonConfig json,
            ReportPortalConfig reportPortal
    ) {
        public static ReportingConfig disabled() {
            return new ReportingConfig(
                    new JsonConfig(false, "TEST_RESULTS/json"),
                    new ReportPortalConfig(false, "", "", "hag-project", "")
            );
        }
        public record JsonConfig(boolean enabled, String outputDir) {}
        public record ReportPortalConfig(boolean enabled, String endpoint, String apiToken, String project, String launchName) {}
    }
}
