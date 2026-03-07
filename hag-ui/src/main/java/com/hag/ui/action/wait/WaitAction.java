package com.hag.ui.action.wait;

import com.hag.core.context.ExecutionContext;
import com.hag.core.dispatcher.descriptor.ActionDescriptor;
import com.hag.core.model.Step;
import com.hag.core.result.ExecutionResult;
import com.hag.ui.action.UiAction;
import com.hag.ui.locator.LocatorResolver;
import com.hag.ui.util.UiDriverExtractor;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public final class WaitAction implements UiAction {

    @Override
    public String name() {
        return "WAIT";
    }

    @Override
    public ExecutionResult execute(
            Step step,
            ActionDescriptor descriptor,
            ExecutionContext context
    ) {

        if (step.getRecipient() == null
                || step.getRecipient().isBlank()) {

            return ExecutionResult.failure(
                    "WAIT requires recipient (locator key)"
            );
        }

        final String locatorKey =
                step.getRecipient().trim();

        final int timeout;
        final WaitCondition condition;

        try {
            timeout = resolveTimeout(descriptor, context);
            condition = resolveCondition(descriptor);
        } catch (IllegalArgumentException ex) {
            return ExecutionResult.failure(ex.getMessage());
        }

        try {

            WebDriver driver =
                    UiDriverExtractor.requireDriver(
                            context.getUiAdapter()
                    );

            By by =
                    LocatorResolver.resolve(locatorKey);

            WebDriverWait wait =
                    new WebDriverWait(
                            driver,
                            Duration.ofSeconds(timeout)
                    );

            switch (condition) {

                case PRESENCE ->
                        wait.until(
                                ExpectedConditions
                                        .presenceOfElementLocated(by)
                        );

                case VISIBLE ->
                        wait.until(
                                ExpectedConditions
                                        .visibilityOfElementLocated(by)
                        );

                case CLICKABLE ->
                        wait.until(
                                ExpectedConditions
                                        .elementToBeClickable(by)
                        );

                case INVISIBLE ->
                        wait.until(
                                ExpectedConditions
                                        .invisibilityOfElementLocated(by)
                        );

                case TEXT_PRESENT -> {
                    String expectedText = step.getKey() != null
                            ? step.getKey().trim()
                            : "";
                    wait.until(
                            ExpectedConditions
                                    .textToBePresentInElementLocated(by, expectedText)
                    );
                }
            }

            return ExecutionResult.success();

        } catch (Exception ex) {

            return ExecutionResult.failure(
                    "WAIT failed for locator [" +
                            locatorKey +
                            "] with condition [" +
                            condition +
                            "] and timeout [" +
                            timeout +
                            "s]: " + ex.getMessage()
            );
        }
    }

    private int resolveTimeout(
            ActionDescriptor descriptor,
            ExecutionContext context
    ) {

        if (context.getConfig() == null) {
            throw new IllegalStateException(
                    "FrameworkConfig not set in ExecutionContext"
            );
        }

        String timeoutValue =
                descriptor.getParameter("timeout");

        if (timeoutValue == null) {
            return context.getConfig()
                    .getDefaultWaitTimeoutSeconds();
        }

        try {
            int parsed = Integer.parseInt(timeoutValue);

            if (parsed <= 0) {
                throw new IllegalArgumentException(
                        "WAIT timeout must be positive"
                );
            }

            return parsed;

        } catch (NumberFormatException ex) {

            throw new IllegalArgumentException(
                    "Invalid WAIT timeout value: "
                            + timeoutValue
            );
        }
    }

    private WaitCondition resolveCondition(
            ActionDescriptor descriptor
    ) {

        String value =
                descriptor.getParameter("condition");

        if (value == null || value.isBlank()) {
            return WaitCondition.VISIBLE;
        }

        try {
            return WaitCondition.from(value);
        } catch (Exception ex) {

            throw new IllegalArgumentException(
                    "Invalid WAIT condition: " + value
            );
        }
    }
}