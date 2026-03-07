package com.hag.api.bootstrap;

import com.hag.api.action.AssertResponseAction;
import com.hag.api.action.AssertStatusAction;
import com.hag.api.action.SendRequestAction;
import com.hag.api.action.StoreDataResponseAction;
import com.hag.api.adapter.RestAssuredApiAdapter;
import com.hag.core.dispatcher.ActionRegistry;

/**
 * ApiBootstrap
 *
 * <p>Registers all REST API actions into the {@link ActionRegistry}
 * and creates the {@link RestAssuredApiAdapter}.
 *
 * <p>Called once during framework startup before any tests run.
 */
public final class ApiBootstrap {

    private ApiBootstrap() {}

    /**
     * Registers all API-layer actions.
     *
     * @param registry the central action registry
     */
    public static void registerApiActions(ActionRegistry registry) {

        // ── Request execution ────────────────────────────────────────────
        registry.register(new SendRequestAction());       // SEND_REQUEST

        // ── Assertions ───────────────────────────────────────────────────
        registry.register(new AssertStatusAction());      // ASSERT_STATUS / :NOT / :2XX / :4XX / :5XX
        registry.register(new AssertResponseAction());    // ASSERT_RESPONSE / :CONTAINS / :NOT_EQUALS / :NOT_NULL / :NULL / :HEADER

        // ── Data extraction ──────────────────────────────────────────────
        registry.register(new StoreDataResponseAction()); // STORE_DATA:RESPONSE / :HEADER / :STATUS
    }

    /**
     * Creates a default {@link RestAssuredApiAdapter} instance
     * with relaxed HTTPS validation (suitable for test environments).
     *
     * @return configured adapter
     */
    public static RestAssuredApiAdapter createAdapter() {
        return new RestAssuredApiAdapter(true);
    }
}
