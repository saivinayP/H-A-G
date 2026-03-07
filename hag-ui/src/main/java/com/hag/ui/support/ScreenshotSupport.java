package com.hag.ui.support;

import com.hag.core.adapter.UiAdapter;
import com.hag.core.config.FrameworkConfig;
import com.hag.ui.util.UiDriverExtractor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public final class ScreenshotSupport {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS");

    private ScreenshotSupport() {}

    public static Optional<Path> capture(
            UiAdapter adapter,
            FrameworkConfig config,
            String testName,
            int stepIndex
    ) {

        if (adapter == null || config == null) {
            return Optional.empty();
        }

        try {

            WebDriver driver =
                    UiDriverExtractor.requireDriver(adapter);

            if (!(driver instanceof TakesScreenshot screenshotDriver)) {
                return Optional.empty();
            }

            File source =
                    screenshotDriver.getScreenshotAs(
                            OutputType.FILE
                    );

            String timestamp =
                    LocalDateTime.now()
                            .format(FORMATTER);

            String sanitizedTestName =
                    sanitize(testName);

            Path directory =
                    Path.of(config.getScreenshotDirectory());

            Path target =
                    directory.resolve(
                            sanitizedTestName
                                    + "_step"
                                    + stepIndex
                                    + "_"
                                    + timestamp
                                    + ".png"
                    );

            Files.createDirectories(directory);

            Files.copy(
                    source.toPath(),
                    target,
                    StandardCopyOption.REPLACE_EXISTING
            );

            return Optional.of(target);

        } catch (Exception ex) {
            return Optional.empty();
        }
    }

    private static String sanitize(String input) {

        if (input == null || input.isBlank()) {
            return "unknown_test";
        }

        return input.replaceAll("[^a-zA-Z0-9-_]", "_");
    }
}