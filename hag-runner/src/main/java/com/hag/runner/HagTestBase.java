package com.hag.runner;

import com.hag.api.adapter.RestAssuredApiAdapter;
import com.hag.core.context.ExecutionContext;
import com.hag.core.engine.ExecutionEngine;
import com.hag.db.adapter.JdbcDbAdapter;
import com.hag.db.bootstrap.DbBootstrap;
import com.hag.runner.bootstrap.FrameworkBootstrap;
import com.hag.runner.config.ConfigLoader.DbConnectionConfig;
import com.hag.runner.ui.SeleniumArtifactProvider;
import com.hag.ui.adapter.SeleniumUiAdapter;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * HagTestBase — TestNG base class for all H-A-G test classes.
 *
 * <h3>Parallel execution model</h3>
 * <ul>
 *   <li>One {@link ExecutionEngine} is shared across all threads (thread-safe)</li>
 *   <li>Each thread has its own {@link ExecutionContext} + WebDriver</li>
 *   <li>Browser is created in {@link #setUpTest()} and closed in {@link #tearDownTest()}</li>
 * </ul>
 *
 * <h3>Usage</h3>
 * <pre>
 * public class LoginTest extends HagTestBase {
 *
 *     &#64;BeforeMethod
 *     public void before() { setUpTest(); }   // or setUpTest("firefox", true)
 *
 *     &#64;Test
 *     public void verifyLogin() {
 *         runTest("Valid Login", "tests/login/valid_login.csv");
 *     }
 * }
 * </pre>
 */
public abstract class HagTestBase {

    private static final Logger LOG = LoggerFactory.getLogger(HagTestBase.class);

    /** Shared execution engine — created once per suite (thread-safe). */
    private static volatile ExecutionEngine sharedEngine;

    /** Per-thread WebDriver instance. */
    private final ThreadLocal<WebDriver> threadDriver = new ThreadLocal<>();

    /** Per-thread execution context. */
    private final ThreadLocal<ExecutionContext> threadContext = new ThreadLocal<>();

    // ── Suite lifecycle ───────────────────────────────────────────────────

    @BeforeSuite(alwaysRun = true)
    public synchronized void setUpSuite() {
        if (sharedEngine == null) {
            SeleniumArtifactProvider artifactProvider = new SeleniumArtifactProvider(
                    () -> {
                        WebDriver d = threadDriver.get();
                        return d != null ? new SeleniumUiAdapter(d) : null;
                    }
            );
            sharedEngine = FrameworkBootstrap.createEngine(artifactProvider);
            LOG.info("HAG → Suite setup complete");
        }
    }

    @AfterSuite(alwaysRun = true)
    public void tearDownSuite() {
        LOG.info("HAG → Suite teardown complete");
    }

    // ── Test lifecycle ────────────────────────────────────────────────────

    /**
     * Creates a WebDriver and wires a fresh {@link ExecutionContext} for this thread.
     *
     * @param browser  browser: {@code chrome} | {@code firefox} | {@code edge}
     * @param headless run without a visible window
     */
    protected void setUpTest(String browser, boolean headless) {
        WebDriver driver = createDriver(browser, headless);
        threadDriver.set(driver);

        ExecutionContext context = new ExecutionContext();
        context.setUiAdapter(new SeleniumUiAdapter(driver));
        context.setApiAdapter(new RestAssuredApiAdapter(true));

        // Wire DB if configured
        String projectRoot = System.getProperty("user.dir");
        DbConnectionConfig dbCfg = FrameworkBootstrap.loadDbConfig(projectRoot);
        if (dbCfg.url() != null && !dbCfg.url().isBlank()) {
            context.setDbAdapter(DbBootstrap.createAdapter(
                    dbCfg.url(), dbCfg.username(), dbCfg.password()
            ));
        }

        threadContext.set(context);
        LOG.info("HAG → Test context ready [thread={}]", Thread.currentThread().getName());
    }

    /** Convenience overload — defaults to Chrome, non-headless. */
    protected void setUpTest() {
        setUpTest("chrome", false);
    }

    @AfterMethod(alwaysRun = true)
    public void tearDownTest(ITestResult result) {
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
                    "No ExecutionContext — call setUpTest() in @BeforeMethod first."
            );
        }
        if (sharedEngine == null) {
            throw new IllegalStateException("Engine not ready — setUpSuite() was not called.");
        }
        Path testFile = resolveTestPath(csvPath);
        LOG.info("HAG → Running [{}] → {}", testName, testFile);
        sharedEngine.execute(testName, testFile, context);
    }

    // ── Accessors ─────────────────────────────────────────────────────────

    protected ExecutionContext getContext() { return threadContext.get(); }
    protected WebDriver        getDriver()  { return threadDriver.get();  }

    // ── Helpers ───────────────────────────────────────────────────────────

    private WebDriver createDriver(String browser, boolean headless) {
        return switch (browser.toLowerCase()) {
            case "firefox" -> {
                WebDriverManager.firefoxdriver().setup();
                FirefoxOptions opts = new FirefoxOptions();
                if (headless) opts.addArguments("--headless");
                yield new FirefoxDriver(opts);
            }
            case "edge" -> {
                WebDriverManager.edgedriver().setup();
                EdgeOptions opts = new EdgeOptions();
                if (headless) opts.addArguments("--headless");
                yield new EdgeDriver(opts);
            }
            default -> {   // chrome
                WebDriverManager.chromedriver().setup();
                ChromeOptions opts = new ChromeOptions();
                if (headless) opts.addArguments("--headless", "--no-sandbox", "--disable-dev-shm-usage");
                yield new ChromeDriver(opts);
            }
        };
    }

    private Path resolveTestPath(String csvPath) {
        Path p = Paths.get(csvPath);
        return p.isAbsolute() ? p : Paths.get(System.getProperty("user.dir")).resolve(p);
    }
}
