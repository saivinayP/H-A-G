package com.hag.core.context;

import com.hag.core.adapter.ApiAdapter;
import com.hag.core.adapter.DbAdapter;
import com.hag.core.adapter.UiAdapter;
import com.hag.core.config.FrameworkConfig;
import com.hag.core.resolver.TestDataResolver;
import com.hag.core.result.ExecutionResult;

import java.util.concurrent.atomic.AtomicInteger;


/**
 * ExecutionContext
 *
 * Holds runtime state for a single test execution.
 *
 * Responsibilities:
 *  - Scoped data storage
 *  - Placeholder resolution
 *  - Adapter access
 *  - Step indexing
 *  - Last result tracking
 *
 * Does NOT:
 *  - Execute business logic
 *  - Resolve test data files directly
 */
public class ExecutionContext {

    private final AtomicInteger currentStepIndex =
            new AtomicInteger(0);

    private final DataStore dataStore =
            new DataStore();

    private ExecutionResult lastResult;

    private UiAdapter uiAdapter;
    private ApiAdapter apiAdapter;
    private DbAdapter dbAdapter;
    private TestDataResolver testDataResolver;
    private FrameworkConfig config;

    /* ==========================================================
       Placeholder Resolution
       ========================================================== */

    /**
     * Resolves variable placeholders in {@code input}.
     *
     * <p>Supports:
     * <ul>
     *   <li>{@code ${VAR}} — global lookup</li>
     *   <li>{@code ${SCOPE:VAR}} — scoped lookup</li>
     *   <li>{@code text ${VAR} text} — embedded tokens in a longer string</li>
     *   <li>{@code ${A}_${B}} — multiple tokens in one value</li>
     * </ul>
     *
     * <p>Delegates to {@link ValueInterpolator} for consistent multi-token handling.
     * Returns the input unchanged if no {@code ${...}} tokens are found.
     *
     * @throws IllegalStateException if any token cannot be resolved
     */
    public Object resolveValue(String input) {
        if (input == null) return null;
        // ValueInterpolator handles all token patterns via regex;
        // if the input has no ${...} it is returned as-is (fast path inside interpolate)
        return ValueInterpolator.interpolate(input.trim(), this);
    }

    /* ==========================================================
       Step Index Handling
       ========================================================== */

    public int nextStepIndex() {
        return currentStepIndex.incrementAndGet();
    }

    public int getCurrentStepIndex() {
        return currentStepIndex.get();
    }

    /* ==========================================================
       Data Store
       ========================================================== */

    public DataStore getDataStore() {
        return dataStore;
    }

    public void reset() {
        currentStepIndex.set(0);
        dataStore.clear();
        lastResult = null;
    }

    /* ==========================================================
       Last Execution Result
       ========================================================== */

    public void setLastResult(ExecutionResult lastResult) {
        this.lastResult = lastResult;
    }

    public ExecutionResult getLastResult() {
        return lastResult;
    }

    /* ==========================================================
       Adapter Accessors
       ========================================================== */

    public UiAdapter getUiAdapter() {
        return uiAdapter;
    }

    public void setUiAdapter(UiAdapter uiAdapter) {
        this.uiAdapter = uiAdapter;
    }

    public ApiAdapter getApiAdapter() {
        return apiAdapter;
    }

    public void setApiAdapter(ApiAdapter apiAdapter) {
        this.apiAdapter = apiAdapter;
    }

    public DbAdapter getDbAdapter() {
        return dbAdapter;
    }

    public void setDbAdapter(DbAdapter dbAdapter) {
        this.dbAdapter = dbAdapter;
    }

    public TestDataResolver getTestDataResolver() {
        return testDataResolver;
    }

    public void setTestDataResolver(
            TestDataResolver testDataResolver
    ) {
        this.testDataResolver = testDataResolver;
    }

    public FrameworkConfig getConfig() {
        return config;
    }

    public void setConfig(FrameworkConfig config) {
        this.config = config;
    }

    /* ==========================================================
       Runtime Validation
       ========================================================== */

    /**
     * Validates that the mandatory framework components are configured.
     *
     * <p><b>Adapter presence is NOT checked here.</b>
     * UI/API/DB adapters are optional and validated lazily inside each
     * action that requires them. This allows UI-only tests to run without
     * configuring an {@code ApiAdapter} or {@code DbAdapter}.
     *
     * <p>Called once by the execution engine before the test begins.
     */
    public void validateConfiguration() {

        if (testDataResolver == null) {
            throw new IllegalStateException(
                    "TestDataResolver is not configured in ExecutionContext"
            );
        }

        if (config == null) {
            throw new IllegalStateException(
                    "FrameworkConfig is not configured in ExecutionContext"
            );
        }
    }

    /** @return {@code true} when a UiAdapter has been registered. */
    public boolean hasUiAdapter()  { return uiAdapter  != null; }

    /** @return {@code true} when an ApiAdapter has been registered. */
    public boolean hasApiAdapter() { return apiAdapter != null; }

    /** @return {@code true} when a DbAdapter has been registered. */
    public boolean hasDbAdapter()  { return dbAdapter  != null; }
}