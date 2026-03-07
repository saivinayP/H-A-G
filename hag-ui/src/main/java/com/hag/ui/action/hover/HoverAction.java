package com.hag.ui.action.hover;

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
import org.openqa.selenium.interactions.Actions;

/**
 * HOVER
 *
 * Moves the mouse pointer over an element (mouseover).
 * Useful for triggering tooltip or dropdown menus.
 *
 * CSV usage:
 *   Action | Recipient      | Source | Key
 *   HOVER  | Page.Element   |        |
 */
public final class HoverAction implements UiAction {

    @Override
    public String name() {
        return "HOVER";
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
                    "HOVER requires recipient (locator key)"
            );
        }

        try {

            WebDriver driver =
                    UiDriverExtractor.requireDriver(context.getUiAdapter());

            By by = LocatorResolver.resolve(step.getRecipient());
            WebElement element = driver.findElement(by);

            new Actions(driver)
                    .moveToElement(element)
                    .perform();

            return ExecutionResult.success();

        } catch (Exception ex) {
            return ExecutionResult.failure(
                    "HOVER failed for locator ["
                            + step.getRecipient()
                            + "]: " + ex.getMessage()
            );
        }
    }
}
