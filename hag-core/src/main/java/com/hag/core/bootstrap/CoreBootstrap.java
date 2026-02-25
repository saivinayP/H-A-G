package com.hag.core.bootstrap;

import com.hag.core.dispatcher.ActionRegistry;
import com.hag.core.dispatcher.DefaultActionDispatcher;
import com.hag.core.dispatcher.DefaultActionRegistry;
import com.hag.core.engine.DefaultExecutionEngine;
import com.hag.core.engine.ExecutionEngine;
import com.hag.core.executor.LogAction;
import com.hag.core.executor.SetContextAction;
import com.hag.core.parser.CsvTestParser;
import com.hag.core.parser.IncludeResolver;
import com.hag.core.reporting.engine.EventPublisher;

/**
 * CoreBootstrap
 *
 * Responsible for:
 *  - Creating registry implementation
 *  - Registering core actions
 *  - Freezing registry
 *  - Wiring dispatcher
 *  - Wiring execution engine
 *
 * This class is the only place where concrete implementations
 * are assembled together.
 */
public final class CoreBootstrap {

    private CoreBootstrap() {}

    public static ExecutionEngine createEngine(
            EventPublisher eventPublisher,
            CsvTestParser parser,
            IncludeResolver includeResolver
    ) {

        // 1️⃣ Create registry implementation
        ActionRegistry registry =
                new DefaultActionRegistry();

        // 2️⃣ Register core-safe actions
        registerCoreActions(registry);

        // 3️⃣ Freeze registry before execution
        registry.freeze();

        // 4️⃣ Create dispatcher
        DefaultActionDispatcher dispatcher =
                new DefaultActionDispatcher(registry);

        // 5️⃣ Create execution engine
        return new DefaultExecutionEngine(
                eventPublisher,
                dispatcher,
                parser,
                includeResolver
        );
    }

    private static void registerCoreActions(
            ActionRegistry registry
    ) {
        registry.register(new LogAction());
        registry.register(new SetContextAction());
    }

}