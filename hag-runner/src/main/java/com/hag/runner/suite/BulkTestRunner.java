package com.hag.runner.suite;

import com.hag.runner.HagTestBase;
import com.hag.runner.config.ConfigLoader;
import com.hag.runner.suite.SuiteDiscovery.TestScenario;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

/**
 * BulkTestRunner — auto-discovers all CSV tests under configured scan roots
 * and executes them as a single parameterised TestNG suite.
 *
 * <h3>How it works</h3>
 * <ol>
 *   <li>On suite start, {@link #setUpSuite()} (inherited) bootstraps the H-A-G engine.</li>
 *   <li>{@link #scenarioProvider()} uses {@link SuiteDiscovery} to scan the {@code tests/}
 *       directory and returns {@code [testName, csvPath, group]} rows.</li>
 *   <li>The single {@code @Test} method {@link #runScenario(String, String, String)}
 *       executes each discovered CSV via {@link #runTest(String, String)}.</li>
 * </ol>
 *
 * <h3>Grouping</h3>
 * Tests are grouped automatically by their parent folder name.  Use TestNG's
 * {@code -Dgroups=smoke} / {@code -Dgroups=api} / {@code -Dgroups=regression} system
 * property or specify groups in {@code testng-bulk.xml} to run a subset.
 *
 * <h3>Scan roots</h3>
 * Configured via {@code runner.config.yml}:
 * <pre>
 * test-suite:
 *   scan-roots:
 *     - tests
 * </pre>
 *
 * <h3>Usage</h3>
 * Add to {@code testng.xml}:
 * <pre>{@code
 * <test name="Bulk Suite">
 *   <classes>
 *     <class name="com.hag.runner.suite.BulkTestRunner"/>
 *   </classes>
 * </test>
 * }</pre>
 */
public class BulkTestRunner extends HagTestBase {

    private static final Logger LOG = LoggerFactory.getLogger(BulkTestRunner.class);

    @BeforeMethod(alwaysRun = true)
    public void before() {
        setUpTest();
    }

    /**
     * Discovers all CSV files under the configured scan roots.
     *
     * <p>Returns a two-dimensional array where each row is:
     * {@code [testName, csvAbsolutePath, group]}
     */
    @DataProvider(name = "scenarios", parallel = true)
    public Object[][] scenarioProvider() {
        String projectRoot = System.getProperty("user.dir");
        List<TestScenario> scenarios = discoverScenarios(projectRoot);

        Object[][] data = new Object[scenarios.size()][3];
        for (int i = 0; i < scenarios.size(); i++) {
            TestScenario s = scenarios.get(i);
            data[i][0] = s.testName();
            data[i][1] = s.csvPath().toAbsolutePath().toString();
            data[i][2] = s.group();
        }
        return data;
    }

    /**
     * Executes a single discovered CSV scenario.
     *
     * <p>The {@code group} parameter is included in the test signature so that
     * TestNG's {@code -Dgroups=...} filtering can be applied at the method level.
     */
    @Test(dataProvider = "scenarios", groups = "all")
    public void runScenario(String testName, String csvPath, String group) {
        LOG.info("HAG → [{}] group={} path={}", testName, group, csvPath);
        runTest(testName, csvPath);
    }

    // ── Internal ─────────────────────────────────────────────────────────

    private List<TestScenario> discoverScenarios(String projectRoot) {
        // Read scan-root from runner.config.yml; default to "tests"
        ConfigLoader.RunnerConfig cfg = ConfigLoader.loadRunnerConfig(projectRoot);
        String scanRootStr = System.getProperty("hag.test.root",
                (cfg != null && cfg.scanRoot() != null) ? cfg.scanRoot() : "tests");

        Path scanRoot = Paths.get(projectRoot).resolve(scanRootStr);
        LOG.info("HAG BulkRunner → scanning: {}", scanRoot.toAbsolutePath());

        List<TestScenario> all = SuiteDiscovery.discover(scanRoot, null);
        if (all.isEmpty()) {
            LOG.warn("HAG BulkRunner → no CSV tests found under: {}", scanRoot);
            return Collections.emptyList();
        }
        return all;
    }
}
