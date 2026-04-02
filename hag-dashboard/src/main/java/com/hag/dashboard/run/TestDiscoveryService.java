package com.hag.dashboard.run;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

/**
 * Scans hag-resource/tests for CSV test files and reads url.config.yml
 * to discover available environments.
 */
@Service
public class TestDiscoveryService {

    private static final YAMLMapper YAML = new YAMLMapper();

    @Value("${hag.test-root:../hag-resource/tests}")
    private String testRoot;

    @Value("${hag.config-dir:..}")
    private String configDir;

    // ── Test tree ────────────────────────────────────────────

    /**
     * Returns a nested list representing the test directory tree.
     * Each node has: name, path (relative), type ("directory" or "test"), children.
     */
    public List<Map<String, Object>> getTestTree() {
        Path root = Paths.get(testRoot).toAbsolutePath().normalize();
        if (!Files.isDirectory(root)) return List.of();
        return buildTree(root, root);
    }

    private List<Map<String, Object>> buildTree(Path dir, Path root) {
        List<Map<String, Object>> nodes = new ArrayList<>();
        File[] children = dir.toFile().listFiles();
        if (children == null) return nodes;

        Arrays.sort(children, Comparator.comparing(File::getName));

        for (File child : children) {
            if (child.isHidden() || child.getName().startsWith("_")) continue;

            Map<String, Object> node = new LinkedHashMap<>();
            node.put("name", child.getName());
            node.put("path", root.relativize(child.toPath()).toString().replace('\\', '/'));

            if (child.isDirectory()) {
                node.put("type", "directory");
                node.put("children", buildTree(child.toPath(), root));
            } else if (child.getName().endsWith(".csv")) {
                node.put("type", "test");
            } else {
                continue; // skip non-CSV files
            }
            nodes.add(node);
        }
        return nodes;
    }

    // ── Environment list from url.config.yml ─────────────────

    public List<String> getEnvironments() {
        Path configPath = Paths.get(configDir).resolve("url.config.yml").toAbsolutePath().normalize();
        if (!Files.exists(configPath)) return List.of("dev");

        try {
            JsonNode root = YAML.readTree(configPath.toFile());
            JsonNode envs = root.path("environments");
            if (envs.isMissingNode()) return List.of("dev");

            List<String> names = new ArrayList<>();
            envs.fieldNames().forEachRemaining(names::add);
            return names;
        } catch (IOException e) {
            return List.of("dev");
        }
    }

    // ── Runner config defaults ──────────────────────────────

    public Map<String, String> getRunnerDefaults() {
        Path configPath = Paths.get(configDir).resolve("runner.config.yml").toAbsolutePath().normalize();
        Map<String, String> defaults = new LinkedHashMap<>();
        defaults.put("browser", "chrome");
        defaults.put("headless", "false");
        defaults.put("mode", "local");
        defaults.put("gridUrl", "");
        defaults.put("threadCount", "1");
        defaults.put("screenshotLevel", "AT_FAILED_STEP");

        if (!Files.exists(configPath)) return defaults;

        try {
            JsonNode root = YAML.readTree(configPath.toFile());
            JsonNode browser = root.path("browser");
            JsonNode execution = root.path("execution");
            JsonNode screenshots = root.path("screenshots");

            if (!browser.path("type").isMissingNode())
                defaults.put("browser", browser.path("type").asText("chrome"));
            if (!browser.path("headless").isMissingNode())
                defaults.put("headless", String.valueOf(browser.path("headless").asBoolean(false)));
            if (!execution.path("mode").isMissingNode())
                defaults.put("mode", execution.path("mode").asText("local"));
            if (!execution.path("grid-url").isMissingNode())
                defaults.put("gridUrl", execution.path("grid-url").asText(""));
            if (!execution.path("thread-count").isMissingNode())
                defaults.put("threadCount", String.valueOf(execution.path("thread-count").asInt(1)));
            if (!screenshots.path("level").isMissingNode())
                defaults.put("screenshotLevel", screenshots.path("level").asText("AT_FAILED_STEP"));
        } catch (IOException ignored) {}

        return defaults;
    }
}
