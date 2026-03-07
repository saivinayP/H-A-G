package com.hag.ui.action.scroll;

import com.hag.core.context.ExecutionContext;
import com.hag.core.dispatcher.descriptor.ActionDescriptor;
import com.hag.core.model.Step;
import com.hag.core.result.ExecutionResult;
import com.hag.ui.action.UiAction;
import com.hag.ui.locator.LocatorResolver;
import com.hag.ui.util.UiDriverExtractor;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * SCROLL
 *
 * Scrolls the page to bring an element or position into view.
 *
 * CSV usage:
 *   Action        | Recipient      | Source | Key
 *   SCROLL        | Page.Element   |        |         ← scroll to element
 *   SCROLL:top    |                |        |         ← scroll to page top
 *   SCROLL:bottom |                |        |         ← scroll to page bottom
 *
 * Flags:
 *   top    — scroll to top of page
 *   bottom — scroll to bottom of page
 *   (none) — scroll to recipient element (default)
 */
public final class ScrollAction implements UiAction {

    @Override
    public String name() {
        return "SCROLL";
    }

    @Override
    public ExecutionResult execute(
            Step step,
            ActionDescriptor descriptor,
            ExecutionContext context
    ) {

        try {

            WebDriver driver =
                    UiDriverExtractor.requireDriver(context.getUiAdapter());

            JavascriptExecutor js = (JavascriptExecutor) driver;

            if (descriptor.hasFlag("top")) {
                js.executeScript("window.scrollTo(0, 0);");
                return ExecutionResult.success();
            }

            if (descriptor.hasFlag("bottom")) {
                js.executeScript(
                        "window.scrollTo(0, document.body.scrollHeight);"
                );
                return ExecutionResult.success();
            }

            // Default: scroll to element
            if (step.getRecipient() == null
                    || step.getRecipient().isBlank()) {
                return ExecutionResult.failure(
                        "SCROLL requires recipient (locator key) " +
                                "unless :top or :bottom flag is used"
                );
            }

            By by = LocatorResolver.resolve(step.getRecipient());
            WebElement element = driver.findElement(by);

            js.executeScript(
                    "arguments[0].scrollIntoView({block:'center', inline:'nearest'});",
                    element
            );

            return ExecutionResult.success();

        } catch (Exception ex) {
            return ExecutionResult.failure(
                    "SCROLL failed: " + ex.getMessage()
            );
        }
    }
}
