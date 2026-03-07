package com.hag.ui.action.click;

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
 * CLICK action — left click, double-click, right-click, or click-and-hold.
 *
 * <pre>
 *   CLICK,Page.Element,,          → standard left click
 *   CLICK:DOUBLE,Page.Element,,   → double click
 *   CLICK:RIGHT,Page.Element,,    → context-menu (right) click
 *   CLICK:HOLD,Page.Element,,     → click and hold (no release)
 * </pre>
 */
public final class ClickAction implements UiAction {

    @Override
    public String name() {
        return "CLICK";
    }

    @Override
    public ExecutionResult execute(Step step, ActionDescriptor descriptor, ExecutionContext context) {

        if (step.getRecipient() == null || step.getRecipient().isBlank()) {
            return ExecutionResult.failure("CLICK requires recipient (locator key)");
        }

        try {
            WebDriver driver = UiDriverExtractor.requireDriver(context.getUiAdapter());
            By by = LocatorResolver.resolve(step.getRecipient());
            WebElement element = driver.findElement(by);
            Actions actions = new Actions(driver);

            if (descriptor.isSubCase("DOUBLE") || descriptor.hasFlag("double")) {
                actions.doubleClick(element).perform();

            } else if (descriptor.isSubCase("RIGHT")) {
                actions.contextClick(element).perform();

            } else if (descriptor.isSubCase("HOLD")) {
                actions.clickAndHold(element).perform();

            } else {
                element.click();
            }

            return ExecutionResult.success();

        } catch (Exception ex) {
            return ExecutionResult.failure(
                    "CLICK failed for locator [" + step.getRecipient() + "]: " + ex.getMessage()
            );
        }
    }
}