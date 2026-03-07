package com.hag.core.bootstrap;

import com.hag.core.dispatcher.ActionRegistry;
import com.hag.core.executor.AssertAction;
import com.hag.core.executor.ChangeDataStoreAction;
import com.hag.core.executor.LogAction;
import com.hag.core.executor.SetContextAction;

/**
 * CoreBootstrap
 *
 * <p>Registers all framework-level (layer-agnostic) actions.
 * Adapter-specific actions are registered by their respective
 * bootstrap classes ({@code UiBootstrap}, {@code ApiBootstrap}, {@code DbBootstrap}).
 */
public final class CoreBootstrap {

    private CoreBootstrap() {}

    public static void registerCoreActions(ActionRegistry registry) {

        // Data management
        registry.register(new ChangeDataStoreAction());   // CHANGE_DATA_STORE / CHANGE_DATA_STORE:DELETE
        registry.register(new SetContextAction());         // SET (legacy alias — kept for backward compat)

        // Assertion
        registry.register(new AssertAction());             // ASSERT / COMPARE (legacy — to be replaced by CompareAction)

        // Utilities
        registry.register(new LogAction());                // LOG
    }
}