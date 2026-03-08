package com.hag.runner.suite;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

/**
 * SuiteDiscovery — scans the test directory tree and discovers CSV test files.
 *
 * <h3>Group assignment rules</h3>
 * <ol>
 *   <li>CSV files are discovered recursively from {@code scanRoot}</li>
 *   <li>The <em>immediate parent folder name</em> becomes the TestNG group
 *       for every CSV in that folder (e.g. {@code tests/login/} → group {@code "login"})</li>
 *   <li>Every discovered test also belongs to the {@code "all"} group</li>
 *   <li>An optional {@code groupMap} in {@code runner.config.yml} can override the folder-name
 *       grouping (e.g. map {@code "login"} → {@code "smoke"})</li>
 * </ol>
 *
 * <h3>Output</h3>
 * Returns a list of {@link TestScenario} records, each describing a single CSV test
 * together with the groups it belongs to.
 */
public final class SuiteDiscovery {

    private static final Logger LOG = LoggerFactory.getLogger(SuiteDiscovery.class);

    private SuiteDiscovery() {}

    /**
     * Scans {@code scanRoot} for {@code .csv} files and builds the scenario list.
     *
     * @param scanRoot  directory to scan (usually the {@code tests/} folder)
     * @param groupAliases optional folder-name → group-name overrides (may be {@code null})
     * @return ordered list of discovered scenarios, empty if none found
     */
    public static List<TestScenario> discover(Path scanRoot, Map<String, String> groupAliases) {

        if (!Files.isDirectory(scanRoot)) {
            LOG.warn("SuiteDiscovery: scan root does not exist or is not a directory: {}", scanRoot);
            return Collections.emptyList();
        }

        Map<String, String> aliases = groupAliases != null ? groupAliases : Collections.emptyMap();
        List<TestScenario> scenarios = new ArrayList<>();

        try (Stream<Path> walk = Files.walk(scanRoot)) {
            walk.filter(Files::isRegularFile)
                .filter(p -> p.getFileName().toString().endsWith(".csv"))
                .sorted()
                .forEach(csv -> {
                    String folderName = csv.getParent().getFileName().toString();
                    String group = aliases.getOrDefault(folderName, folderName);
                    String testName = deriveTestName(csv);
                    scenarios.add(new TestScenario(testName, csv, group));
                    LOG.debug("SuiteDiscovery → {} [group={}]", testName, group);
                });
        } catch (IOException e) {
            throw new RuntimeException("SuiteDiscovery: failed to scan " + scanRoot, e);
        }

        LOG.info("SuiteDiscovery → discovered {} scenarios in: {}", scenarios.size(), scanRoot);
        return Collections.unmodifiableList(scenarios);
    }

    /** Derives a human-readable test name from a CSV file path. */
    private static String deriveTestName(Path csv) {
        String filename = csv.getFileName().toString();
        // Remove .csv extension, replace underscores/hyphens with spaces, title-case words
        String base = filename.substring(0, filename.length() - 4)
                              .replace('_', ' ')
                              .replace('-', ' ');
        return toTitleCase(base);
    }

    private static String toTitleCase(String s) {
        if (s == null || s.isBlank()) return s;
        String[] words = s.split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String w : words) {
            if (!w.isEmpty()) {
                if (!sb.isEmpty()) sb.append(' ');
                sb.append(Character.toUpperCase(w.charAt(0)));
                if (w.length() > 1) sb.append(w.substring(1).toLowerCase());
            }
        }
        return sb.toString();
    }

    /**
     * Immutable descriptor for a discovered test scenario.
     *
     * @param testName  human-readable display name
     * @param csvPath   absolute path to the CSV file
     * @param group     primary TestNG group (derived from folder name or alias)
     */
    public record TestScenario(String testName, Path csvPath, String group) {}
}
