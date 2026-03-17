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
import com.hag.core.reporting.engine.HtmlReportEngine;
import com.hag.core.reporting.engine.JsonEventsReporter;
import com.hag.core.reporting.engine.ReportEngine;
import com.hag.core.reporting.engine.ReportPortalEngine;
import com.hag.db.bootstrap.DbBootstrap;
import com.hag.runner.config.ConfigLoader;
import com.hag.runner.config.ConfigLoader.ReportingConfig;
import com.hag.ui.bootstrap.UiBootstrap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * FrameworkBootstrap — single composition root that wires the full H-A-G stack.
 *
 * <h3>Reporting engines (Phase 3)</h3>
 * Two additional engines are conditionally wired based on {@code runner.config.yml}:
 * <ul>
 *   <li>{@link JsonEventsReporter} — enabled when {@code reporting.json.enabled: true}</li>
 *   <li>{@link ReportPortalEngine} — enabled when {@code reporting.report-portal.enabled: true}</li>
 * </ul>
 * Both default to <em>disabled</em> so existing runs are unaffected.
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
        FrameworkConfig  config    = ConfigLoader.load(projectRoot);
        ReportingConfig  reporting = ConfigLoader.loadReportingConfig(projectRoot);

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

        // ── Report engines ───────────────────────────────────────────────
        String runName = System.getProperty("hag.run.name", "Test Execution");

        List<ReportEngine> engines = new ArrayList<>();
        engines.add(new ConsoleReportEngine());
        engines.add(new HtmlReportEngine(runName));

        if (reporting.json().enabled()) {
            engines.add(new JsonEventsReporter(reporting.json().outputDir(), projectRoot));
            LOG.info("HAG → JsonEventsReporter enabled → output: {}", reporting.json().outputDir());
        }

        if (reporting.reportPortal().enabled()) {
            ReportPortalEngine.Config rpCfg = new ReportPortalEngine.Config(
                    reporting.reportPortal().endpoint(),
                    reporting.reportPortal().apiToken(),
                    reporting.reportPortal().project(),
                    reporting.reportPortal().launchName()
            );
            engines.add(new ReportPortalEngine(rpCfg));
            LOG.info("HAG → ReportPortalEngine enabled → endpoint: {}", reporting.reportPortal().endpoint());
        }

        // ── Event publishing ─────────────────────────────────────────────
        EventPublisher eventPublisher = new DefaultEventPublisher(engines);
        eventPublisher.startSuite();

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