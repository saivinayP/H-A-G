package com.hag.tests.smoke;

import com.hag.runner.HagTestBase;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * LoginTest — smoke-level login scenarios.
 *
 * <p>Extends {@link HagTestBase} — no test code required, just:
 * <ol>
 *   <li>Call {@link #setUpTest()} in {@code @BeforeMethod}</li>
 *   <li>Call {@link #runTest(String, String)} with a display name and CSV path</li>
 * </ol>
 *
 * <h3>Running just these tests</h3>
 * <pre>
 *   mvn test -pl hag-runner -am -Dgroups=smoke
 * </pre>
 */
public class LoginTest extends HagTestBase {

    @BeforeMethod(alwaysRun = true)
    public void setUp() {
        // Uses browser/headless from runner.config.yml (or -Dbrowser / -Dheadless override)
        setUpTest();
    }

    @Test(groups = "smoke", description = "Valid user can log in and see the dashboard")
    public void validUserShouldReachDashboard() {
        runTest("Valid Login", "tests/login/valid_login.csv");
    }

    @Test(groups = "smoke", description = "Locked user sees an error message")
    public void lockedUserShouldSeeError() {
        runTest("Locked User Login", "tests/login/locked_login.csv");
    }
}
