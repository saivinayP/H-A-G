package com.hag.runner.ui;

import com.hag.core.context.ExecutionContext;
import com.hag.core.engine.FailureArtifactProvider;
import com.hag.ui.adapter.SeleniumUiAdapter;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * SeleniumArtifactProvider — takes a screenshot on step failure and saves it
 * to the configured screenshot directory.
 *
 * <p>Uses a {@link Supplier} to access the current thread's {@link SeleniumUiAdapter}
 * so it works correctly with parallel execution.
 */
public final class SeleniumArtifactProvider implements FailureArtifactProvider {

    private static final Logger LOG = LoggerFactory.getLogger(SeleniumArtifactProvider.class);
    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS");

    private final Supplier<SeleniumUiAdapter> adapterSupplier;
    private final String screenshotDir;

    public SeleniumArtifactProvider(Supplier<SeleniumUiAdapter> adapterSupplier) {
        this(adapterSupplier, "target/screenshots");
    }

    public SeleniumArtifactProvider(
            Supplier<SeleniumUiAdapter> adapterSupplier,
            String screenshotDir
    ) {
        this.adapterSupplier = adapterSupplier;
        this.screenshotDir = screenshotDir;
    }

    @Override
    public Optional<Path> capture(String testName, int stepIndex, ExecutionContext context) {
        SeleniumUiAdapter adapter = adapterSupplier.get();
        if (adapter == null) return Optional.empty();

        WebDriver driver = adapter.driver();
        if (driver == null) return Optional.empty();

        try {
            // Ensure directory exists
            Path dir = Paths.get(screenshotDir);
            Files.createDirectories(dir);

            // Build filename
            String safeName = testName.replaceAll("[^a-zA-Z0-9_-]", "_");
            String timestamp = LocalDateTime.now().format(TS);
            String filename = safeName + "_step" + stepIndex + "_" + timestamp + ".png";

            Path target = dir.resolve(filename);

            // Take screenshot
            File src = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            Files.copy(src.toPath(), target);

            LOG.info("HAG → Screenshot saved: {}", target);
            return Optional.of(target);

        } catch (IOException e) {
            LOG.warn("HAG → Screenshot failed: {}", e.getMessage());
            return Optional.empty();
        }
    }
}
