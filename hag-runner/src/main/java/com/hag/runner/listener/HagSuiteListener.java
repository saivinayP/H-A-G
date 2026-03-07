package com.hag.runner.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

/**
 * HagSuiteListener — TestNG listener for suite and test lifecycle events.
 *
 * <p>Logs suite start/finish and each test's pass/fail/skip status
 * to the SLF4J logger. Extend this class to hook into custom reporting.
 */
public final class HagSuiteListener implements ISuiteListener, ITestListener {

    private static final Logger LOG = LoggerFactory.getLogger(HagSuiteListener.class);

    // ── Suite ─────────────────────────────────────────────────────────────

    @Override
    public void onStart(ISuite suite) {
        LOG.info("═══════════════════════════════════════════════");
        LOG.info("  HAG ▶ SUITE STARTED: {}", suite.getName());
        LOG.info("═══════════════════════════════════════════════");
    }

    @Override
    public void onFinish(ISuite suite) {
        LOG.info("═══════════════════════════════════════════════");
        LOG.info("  HAG ■ SUITE FINISHED: {}", suite.getName());
        LOG.info("═══════════════════════════════════════════════");
    }

    // ── Test ──────────────────────────────────────────────────────────────

    @Override
    public void onTestStart(ITestResult result) {
        LOG.info("► TEST START: {}", result.getMethod().getMethodName());
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        LOG.info("✔ TEST PASSED: {} ({}ms)",
                result.getMethod().getMethodName(),
                result.getEndMillis() - result.getStartMillis());
    }

    @Override
    public void onTestFailure(ITestResult result) {
        LOG.error("✘ TEST FAILED: {} — {}",
                result.getMethod().getMethodName(),
                result.getThrowable() != null ? result.getThrowable().getMessage() : "unknown");
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        LOG.warn("⚠ TEST SKIPPED: {}", result.getMethod().getMethodName());
    }

    @Override public void onStart(ITestContext context) {}
    @Override public void onFinish(ITestContext context) {}
}
