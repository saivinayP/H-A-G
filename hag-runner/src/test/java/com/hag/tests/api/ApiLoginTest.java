package com.hag.tests.api;

import com.hag.runner.HagTestBase;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * ApiLoginTest — REST API login scenarios.
 *
 * <p>These tests don't need a browser — they execute API-only CSV flows.
 * No WebDriver is launched (browser launch is still triggered by setUpTest,
 * but can be replaced by a no-browser setup when headless mode is not needed).
 *
 * <h3>Running just API tests</h3>
 * <pre>
 *   mvn test -pl hag-runner -am -Dgroups=api
 * </pre>
 */
public class ApiLoginTest extends HagTestBase {

    @BeforeMethod(alwaysRun = true)
    public void setUp() {
        // Headless Chrome — API tests may still redirect to browser assertions
        setUpTest("chrome", true);
    }

    @Test(groups = "api", description = "API login returns 200 with a valid token")
    public void apiLoginShouldReturn200WithToken() {
        runTest("API Login - Valid User", "tests/api/api_login_test.csv");
    }
}
