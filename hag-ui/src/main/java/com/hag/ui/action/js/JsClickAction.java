package com.hag.ui.action.js;

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
 * JS_CLICK
 *
 * Fires a JavaScript-based click on an element.
 * Use when the standard Selenium click is intercepted or the element is obscured.
 *
 * CSV usage:
 *   Action    | Recipient    | Source | Key
 *   JS_CLICK  | Page.Element |        |
 */
public final class JsClickAction implements UiAction {

    @Override
    public String name() {
        return "JS_CLICK";
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
                    "JS_CLICK requires recipient (locator key)"
            );
        }

        try {

            WebDriver driver =
                    UiDriverExtractor.requireDriver(context.getUiAdapter());

            By by = LocatorResolver.resolve(step.getRecipient());
            WebElement element = driver.findElement(by);

            ((JavascriptExecutor) driver)
                    .executeScript("arguments[0].click();", element);

            return ExecutionResult.success();

        } catch (Exception ex) {
            return ExecutionResult.failure(
                    "JS_CLICK failed for locator ["
                            + step.getRecipient()
                            + "]: " + ex.getMessage()
            );
        }
    }
}
