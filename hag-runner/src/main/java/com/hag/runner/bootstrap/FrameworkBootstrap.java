package com.hag.runner.bootstrap;

import com.hag.api.bootstrap.ApiBootstrap;
import com.hag.core.bootstrap.CoreBootstrap;
import com.hag.core.config.FrameworkConfig;
import com.hag.core.dispatcher.DefaultActionDispatcher;
import com.hag.core.dispatcher.DefaultActionRegistry;
import com.hag.core.engine.DefaultExecutionEngine;
import com.hag.core.engine.ExecutionEngine;
import com.hag.core.engine.FailureArtifactProvider;
import com.hag.core.parser.CsvTestParser;
import com.hag.core.parser.IncludeResolver;
import com.hag.core.reporting.engine.ConsoleReportEngine;
import com.hag.core.reporting.engine.DefaultEventPublisher;
import com.hag.core.reporting.engine.EventPublisher;
import com.hag.db.bootstrap.DbBootstrap;
import com.hag.runner.config.ConfigLoader;
import com.hag.ui.bootstrap.UiBootstrap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * FrameworkBootstrap — single composition root that wires the full H-A-G stack.
 */
public final class FrameworkBootstrap {

    private static final Logger LOG = LoggerFactory.getLogger(FrameworkBootstrap.class);

    private FrameworkBootstrap() {}

    /**
     * Creates a fully-wired {@link ExecutionEngine} from {@code projectRoot}.
     *
     * @param projectRoot      directory containing url/runner/testdata config files
     * @param artifactProvider failure screenshot provider (can be {@code null} for API-only)
     */
    public static ExecutionEngine createEngine(
            String projectRoot,
            FailureArtifactProvider artifactProvider
    ) {
        LOG.info("HAG → Bootstrap starting from: {}", projectRoot);

        // ── Config ───────────────────────────────────────────────────────
        FrameworkConfig config = ConfigLoader.load(projectRoot);

        // ── Action registry ──────────────────────────────────────────────
        DefaultActionRegistry registry = new DefaultActionRegistry();

        CoreBootstrap.registerCoreActions(registry);
        LOG.info("HAG → Core actions registered");

        UiBootstrap.registerUiActions(registry);
        LOG.info("HAG → UI actions registered");

        ApiBootstrap.registerApiActions(registry);
        LOG.info("HAG → API actions registered");

        DbBootstrap.registerDbActions(registry);
        LOG.info("HAG → DB actions registered");

        registry.freeze();

        // ── Dispatcher & parser ──────────────────────────────────────────
        DefaultActionDispatcher dispatcher = new DefaultActionDispatcher(registry);
        CsvTestParser           parser     = new CsvTestParser();

        // ── Event publishing ─────────────────────────────────────────────
        EventPublisher eventPublisher = new DefaultEventPublisher(
                List.of(new ConsoleReportEngine())
        );

        // ── Include resolver ─────────────────────────────────────────────
        IncludeResolver includeResolver = new IncludeResolver(parser, eventPublisher);

        LOG.info("HAG → Bootstrap complete");

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