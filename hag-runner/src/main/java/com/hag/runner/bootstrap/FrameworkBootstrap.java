package com.hag.runner.bootstrap;

import com.hag.core.bootstrap.CoreBootstrap;
import com.hag.core.config.FrameworkConfig;
import com.hag.core.dispatcher.ActionRegistry;
import com.hag.core.dispatcher.DefaultActionDispatcher;
import com.hag.core.dispatcher.DefaultActionRegistry;
import com.hag.core.engine.DefaultExecutionEngine;
import com.hag.core.engine.ExecutionEngine;
import com.hag.core.engine.FailureArtifactProvider;
import com.hag.core.parser.CsvTestParser;
import com.hag.core.parser.IncludeResolver;
import com.hag.core.reporting.engine.EventPublisher;
import com.hag.ui.bootstrap.UiBootstrap;

/**
 * FrameworkBootstrap
 *
 * Single composition root.
 * Wires core + UI + engine.
 */
public final class FrameworkBootstrap {

    private FrameworkBootstrap() {}

    public static ExecutionEngine createEngine(
            EventPublisher eventPublisher,
            CsvTestParser parser,
            IncludeResolver includeResolver,
            FailureArtifactProvider artifactProvider,
            FrameworkConfig config
    ) {

        // 1️⃣ Create registry
        ActionRegistry registry =
                new DefaultActionRegistry();

        // 2️⃣ Register core actions
        CoreBootstrap.registerCoreActions(registry);

        // 3️⃣ Register UI actions
        UiBootstrap.registerUiActions(registry);

        // 4️⃣ Freeze registry before use
        registry.freeze();

        // 5️⃣ Create dispatcher
        DefaultActionDispatcher dispatcher =
                new DefaultActionDispatcher(registry);

        // 6️⃣ Create execution engine
        return new DefaultExecutionEngine(
                eventPublisher,
                dispatcher,
                parser,
                includeResolver,
                artifactProvider,
                config
        );
    }
}