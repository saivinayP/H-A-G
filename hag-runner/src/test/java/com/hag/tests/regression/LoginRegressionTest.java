package com.hag.tests.regression;

import com.hag.runner.HagTestBase;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * LoginRegressionTest — full cross-layer login E2E scenario.
 *
 * <p>Runs the complete flow: API auth → browser UI → DB validation.
 *
 * <h3>Running regression tests</h3>
 * <pre>
 *   mvn test -pl hag-runner -am -Dgroups=regression
 * </pre>
 */
public class LoginRegressionTest extends HagTestBase {

    @BeforeMethod(alwaysRun = true)
    public void setUp() {
        setUpTest();
    }

    @Test(groups = "regression", description = "E2E login: API auth → UI dashboard → DB session record")
    public void loginE2EShouldValidateAllLayers() {
        runTest("Login E2E - All Layers", "tests/e2e/login_e2e_test.csv");
    }
}
