package com.hag.core.bootstrap;

import com.hag.core.dispatcher.ActionRegistry;
import com.hag.core.executor.ChangeDataStoreAction;
import com.hag.core.executor.CompareAction;
import com.hag.core.executor.LogAction;

/**
 * CoreBootstrap — registers framework-level (layer-agnostic) actions.
 *
 * <p>Adapter-specific actions are registered by their respective bootstrap classes:
 * {@code UiBootstrap}, {@code ApiBootstrap}, {@code DbBootstrap}.
 *
 * <h3>Registered actions</h3>
 * <ul>
 *   <li>{@code CHANGE_DATA_STORE} / {@code CHANGE_DATA_STORE:DELETE}</li>
 *   <li>{@code COMPARE:EQUALS} / {@code :NOT_EQUALS} / {@code :CONTAINS} /
 *       {@code :NOT_CONTAINS} / {@code :STARTS_WITH} / {@code :ENDS_WITH} /
 *       {@code :GT} / {@code :LT} / {@code :GTE} / {@code :LTE} / {@code :REGEX}</li>
 *   <li>{@code LOG}</li>
 * </ul>
 */
public final class CoreBootstrap {

    private CoreBootstrap() {}

    public static void registerCoreActions(ActionRegistry registry) {

        // Data management
        registry.register(new ChangeDataStoreAction());

        // Assertion & comparison (11 operators)
        registry.register(new CompareAction());

        // Utilities
        registry.register(new LogAction());
    }
}