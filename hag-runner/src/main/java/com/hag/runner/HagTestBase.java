package com.hag.runner;

import com.hag.api.adapter.RestAssuredApiAdapter;
import com.hag.core.context.ExecutionContext;
import com.hag.core.engine.ExecutionEngine;
import com.hag.db.adapter.JdbcDbAdapter;
import com.hag.db.bootstrap.DbBootstrap;
import com.hag.runner.bootstrap.FrameworkBootstrap;
import com.hag.runner.config.ConfigLoader;
import com.hag.runner.config.ConfigLoader.DbConnectionConfig;
import com.hag.runner.config.ConfigLoader.RunnerConfig;
import com.hag.runner.ui.SeleniumArtifactProvider;
import com.hag.ui.adapter.SeleniumUiAdapter;
import com.hag.ui.locator.LocatorRepository;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;

/**
 * HagTestBase — TestNG base class for all H-A-G test classes.
 *
 * <h3>Parallel execution model</h3>
 * <ul>
 *   <li>One {@link ExecutionEngine} shared across all threads (thread-safe)</li>
 *   <li>Each thread has its own {@link ExecutionContext} + WebDriver</li>
 *   <li>Browser is created in {@link #setUpTest()} and closed in {@link #tearDownTest()}</li>
 * </ul>
 *
 * <h3>Configuration</h3>
 * Browser type and headless mode are read from {@code runner.config.yml} first, then
 * from system properties ({@code -Dbrowser=firefox -Dheadless=true}), then defaulting
 * to Chrome non-headless.
 *
 * <h3>Usage</h3>
 * <pre>
 * public class LoginTest extends HagTestBase {
 *
 *     &#64;BeforeMethod
 *     public void before() { setUpTest(); }
 *
 *     &#64;Test(groups = "smoke")
 *     public void verifyLogin() {
 *         runTest("Valid Login", "tests/login/valid_login.csv");
 *     }
 * }
 * </pre>
 */
public abstract class HagTestBase {

    private static final Logger LOG = LoggerFactory.getLogger(HagTestBase.class);

    // ── Suite-level singletons (initialised once, reset at suite end) ────

    /** Shared execution engine — created once per suite (thread-safe). */
    private static volatile ExecutionEngine sharedEngine;

    /** DB + runner config cached at suite startup, not re-read per test. */
    private static volatile DbConnectionConfig cachedDbConfig;
    private static volatile RunnerConfig       cachedRunnerConfig;

    // ── Per-thread resources ─────────────────────────────────────────────

    /** Per-thread WebDriver instance. */
    private final ThreadLocal<WebDriver> threadDriver = new ThreadLocal<>();

    /** Per-thread execution context. */
    private final ThreadLocal<ExecutionContext> threadContext = new ThreadLocal<>();

    // ── Suite lifecycle ───────────────────────────────────────────────────

    @BeforeSuite(alwaysRun = true)
    public synchronized void setUpSuite() {
        if (sharedEngine != null) return;   // already initialised by another test class

        String projectRoot = System.getProperty("user.dir");
        if (projectRoot.endsWith("hag-runner") || projectRoot.endsWith("hag-runner\\")) {
            projectRoot = java.nio.file.Paths.get(projectRoot).getParent().toString();
        }

        // RUN-1: cache heavy config reads once per suite, not per method
        cachedRunnerConfig = ConfigLoader.loadRunnerConfig(projectRoot);
        cachedDbConfig     = ConfigLoader.loadDbConfig(projectRoot);

        SeleniumArtifactProvider artifactProvider = new SeleniumArtifactProvider(
                () -> {
                    WebDriver d = threadDriver.get();
                    return d != null ? new SeleniumUiAdapter(() -> d) : null;
                }
        );
        sharedEngine = FrameworkBootstrap.createEngine(projectRoot, artifactProvider);

        // Wire LocatorRepository filesystem root from config
        configureLocatorRoot(projectRoot);

        LOG.info("HAG → Suite setup complete (engine ready)");
    }

    /**
     * RUN-2: Reset the shared engine so it can be re-initialised in the next suite
     * run within the same JVM (e.g. when TestNG runs multiple suites in sequence).
     * Also clears the LocatorRepository cache.
     */
    @AfterSuite(alwaysRun = true)
    public synchronized void tearDownSuite() {
        if (sharedEngine != null) {
            // Need to trigger endSuite on EventPublisher (which is encapsulated in DefaultExecutionEngine)
            // But we don't have direct access. Let's cast it since we know the implementation.
            if (sharedEngine instanceof com.hag.core.engine.DefaultExecutionEngine eng) {
                System.out.println("HAG → Triggering EventPublisher.endSuite() across all ReportEngines...");
                eng.getEventPublisher().endSuite();
            } else {
                System.out.println("HAG → sharedEngine is NOT an instance of DefaultExecutionEngine: " + sharedEngine.getClass().getName());
            }
        } else {
            System.out.println("HAG → sharedEngine is null in tearDownSuite, skip ending suite reports");
        }
        
        sharedEngine       = null;
        cachedDbConfig     = null;
        cachedRunnerConfig = null;
        LocatorRepository.clearCache();
        LOG.info("HAG → Suite teardown complete");
    }

    // ── Test lifecycle ────────────────────────────────────────────────────

    /**
     * Creates a WebDriver and wires a fresh {@link ExecutionContext} for this thread.
     *
     * <p>Browser type and headless mode are resolved in order:
     * <ol>
     *   <li>System property ({@code -Dbrowser=...}, {@code -Dheadless=true})</li>
     *   <li>Values from {@code runner.config.yml}</li>
     *   <li>Default: chrome, non-headless</li>
     * </ol>
     */
    protected void setUpTest() {
        // RUN-4: read browser/headless from config, with system-property override
        RunnerConfig runnerCfg = cachedRunnerConfig;
        String  browser  = System.getProperty("browser",
                runnerCfg != null ? runnerCfg.browserType() : "chrome");
        boolean headless = Boolean.parseBoolean(System.getProperty("headless",
                runnerCfg != null ? String.valueOf(runnerCfg.headless()) : "false"));
        String  mode     = System.getProperty("execution.mode",
                runnerCfg != null ? runnerCfg.executionMode() : "local");
        String  gridUrl  = System.getProperty("grid.url",
                runnerCfg != null ? runnerCfg.gridUrl() : "");

        setUpTest(browser, headless, mode, gridUrl);
    }

    /**
     * Creates a WebDriver with explicit browser, headless, mode, and grid settings.
     *
     * @param browser  {@code chrome} | {@code firefox} | {@code edge}
     * @param headless run without a visible window
     * @param mode     {@code local} | {@code remote}
     * @param gridUrl  URL of the remote selenium grid
     */
    protected void setUpTest(String browser, boolean headless, String mode, String gridUrl) {
        // Create a lazy supplier for the WebDriver
        java.util.function.Supplier<WebDriver> lazyDriver = () -> {
            WebDriver d = threadDriver.get();
            if (d == null) {
                d = createDriver(browser, headless, mode, gridUrl);
                threadDriver.set(d);
                
                // Apply implicit wait timeout from config
                RunnerConfig runnerCfg = cachedRunnerConfig;
                if (runnerCfg != null && runnerCfg.timeoutSeconds() > 0) {
                    d.manage().timeouts()
                      .implicitlyWait(Duration.ofSeconds(runnerCfg.timeoutSeconds()));
                }
                LOG.info("HAG → Browser launched [thread={}, browser={}{}]",
                        Thread.currentThread().getName(), browser, headless ? " headless" : "");
            }
            return d;
        };

        ExecutionContext context = new ExecutionContext();
        context.setUiAdapter(new SeleniumUiAdapter(lazyDriver));
        context.setApiAdapter(new RestAssuredApiAdapter(true));
        context.setTestDataResolver(new com.hag.core.resolver.DefaultTestDataResolver());

        // DB-1: wire DB adapter from cached config (close() called in teardown)
        DbConnectionConfig dbCfg = cachedDbConfig;
        if (dbCfg != null && dbCfg.url() != null && !dbCfg.url().isBlank()) {
            context.setDbAdapter(DbBootstrap.createAdapter(
                    dbCfg.url(), dbCfg.username(), dbCfg.password()));
        }

        threadContext.set(context);
        LOG.info("HAG → Test context ready [thread={}]",
                Thread.currentThread().getName());
    }

    /**
     * Cleans up WebDriver and JDBC connection for this thread.
     * DB-1 fix: calls {@link JdbcDbAdapter#close()} to prevent connection leaks.
     */
    @AfterMethod(alwaysRun = true)
    public void tearDownTest(ITestResult result) {
        // DB-1: close JDBC connection to prevent leak
        ExecutionContext ctx = threadContext.get();
        if (ctx != null && ctx.getDbAdapter() instanceof JdbcDbAdapter db) {
            db.close();
        }

        WebDriver driver = threadDriver.get();
        if (driver != null) {
            try { driver.quit(); } catch (Exception e) {
                LOG.warn("HAG → WebDriver quit failed: {}", e.getMessage());
            }
            threadDriver.remove();
        }
        threadContext.remove();
    }

    // ── Test execution ────────────────────────────────────────────────────

    /**
     * Runs a H-A-G CSV test file.
     *
     * @param testName display name shown in reports
     * @param csvPath  path to CSV (relative to project root, or absolute)
     */
    protected void runTest(String testName, String csvPath) {
        ExecutionContext context = threadContext.get();
        if (context == null) {
            throw new IllegalStateException(
                    "No ExecutionContext — call setUpTest() in @BeforeMethod first.");
        }
        if (sharedEngine == null) {
            throw new IllegalStateException("Engine not ready — setUpSuite() was not called.");
        }
        Path testFile = resolveTestPath(csvPath);
        LOG.info("HAG → Running [{}] → {}", testName, testFile);
        sharedEngine.execute(testName, testFile, context);
    }

    protected ExecutionContext getContext() { return threadContext.get(); }
    protected WebDriver        getDriver()  { return threadDriver.get();  }
    
    protected static ExecutionEngine getSharedEngine() {
        return sharedEngine;
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private WebDriver createDriver(String browser, boolean headless, String mode, String gridUrl) {
        boolean isRemote = "remote".equalsIgnoreCase(mode);
        
        return switch (browser.toLowerCase()) {
            case "firefox" -> {
                FirefoxOptions opts = new FirefoxOptions();
                if (headless) opts.addArguments("--headless");
                if (isRemote) {
                    try {
                        yield new RemoteWebDriver(new java.net.URL(gridUrl), opts);
                    } catch (java.net.MalformedURLException e) {
                        throw new RuntimeException("Invalid grid URL: " + gridUrl, e);
                    }
                } else {
                    WebDriverManager.firefoxdriver().setup();
                    // Fallback to older instantiation since FirefoxDriver uses standard Options pattern but the import was removed for RemoteWebDriver.
                    // Instead we will explicitly declare it using the fully qualified name since we removed the import
                    yield new org.openqa.selenium.firefox.FirefoxDriver(opts);
                }
            }
            case "edge" -> {
                EdgeOptions opts = new EdgeOptions();
                if (headless) opts.addArguments("--headless");
                if (isRemote) {
                    try {
                        yield new RemoteWebDriver(new java.net.URL(gridUrl), opts);
                    } catch (java.net.MalformedURLException e) {
                        throw new RuntimeException("Invalid grid URL: " + gridUrl, e);
                    }
                } else {
                    WebDriverManager.edgedriver().setup();
                    yield new EdgeDriver(opts);
                }
            }
            default -> {   // chrome (default)
                ChromeOptions opts = new ChromeOptions();
                if (headless) opts.addArguments("--headless", "--no-sandbox", "--disable-dev-shm-usage");
                if (isRemote) {
                    try {
                        yield new RemoteWebDriver(new java.net.URL(gridUrl), opts);
                    } catch (java.net.MalformedURLException e) {
                        throw new RuntimeException("Invalid grid URL: " + gridUrl, e);
                    }
                } else {
                    WebDriverManager.chromedriver().setup();
                    yield new ChromeDriver(opts);
                }
            }
        };
    }

    private Path resolveTestPath(String csvPath) {
        Path p = Paths.get(csvPath);
        return p.isAbsolute() ? p : Paths.get(System.getProperty("user.dir")).resolve(p);
    }

    private static void configureLocatorRoot(String projectRoot) {
        // Try to get locators path from testdata.config.yml
        try {
            String locPath = ConfigLoader.loadLocatorsPath(projectRoot);
            if (locPath != null && !locPath.isBlank()) {
                LocatorRepository.setLocatorRoot(
                        Paths.get(projectRoot).resolve(locPath).normalize());
            }
        } catch (Exception e) {
            LOG.warn("HAG → Could not configure locator root from config: {}", e.getMessage());
        }
    }
}
