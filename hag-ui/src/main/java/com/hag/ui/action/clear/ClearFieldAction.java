package com.hag.ui.action.clear;

import com.hag.core.context.ExecutionContext;
import com.hag.core.dispatcher.descriptor.ActionDescriptor;
import com.hag.core.model.Step;
import com.hag.core.result.ExecutionResult;
import com.hag.ui.action.UiAction;
import com.hag.ui.locator.LocatorResolver;
import com.hag.ui.util.UiDriverExtractor;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * CLEAR
 *
 * Clears the content of an input or textarea element.
 *
 * CSV usage:
 *   Action | Recipient    | Source | Key
 *   CLEAR  | Page.Element |        |
 */
public final class ClearFieldAction implements UiAction {

    @Override
    public String name() {
        return "CLEAR";
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
                    "CLEAR requires recipient (locator key)"
            );
        }

        try {

            WebDriver driver =
                    UiDriverExtractor.requireDriver(context.getUiAdapter());

            By by = LocatorResolver.resolve(step.getRecipient());
            WebElement element = driver.findElement(by);
            element.clear();

            return ExecutionResult.success();

        } catch (Exception ex) {
            return ExecutionResult.failure(
                    "CLEAR failed for locator ["
                            + step.getRecipient()
                            + "]: " + ex.getMessage()
            );
        }
    }
}
