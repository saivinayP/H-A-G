package com.hag.ui.action.assertion;

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

import java.util.List;

/**
 * ASSERT_VISIBLE
 *
 * Asserts that an element is present in the DOM and visible.
 *
 * CSV usage:
 *   Action                 | Recipient    | Source | Key
 *   ASSERT_VISIBLE         | Page.Element |        |       ← must be visible
 *   ASSERT_VISIBLE:hidden  | Page.Element |        |       ← must NOT be visible
 *
 * Flags:
 *   hidden — asserts the element is hidden / not displayed
 */
public final class AssertVisibleAction implements UiAction {

    @Override
    public String name() {
        return "ASSERT_VISIBLE";
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
                    "ASSERT_VISIBLE requires recipient (locator key)"
            );
        }

        boolean expectHidden = descriptor.hasFlag("hidden");

        try {

            WebDriver driver =
                    UiDriverExtractor.requireDriver(context.getUiAdapter());

            By by = LocatorResolver.resolve(step.getRecipient());
            List<WebElement> elements = driver.findElements(by);

            boolean isVisible = !elements.isEmpty()
                    && elements.get(0).isDisplayed();

            if (expectHidden && isVisible) {
                return ExecutionResult.failure(
                        "ASSERT_VISIBLE:hidden failed — element is visible: ["
                                + step.getRecipient() + "]"
                );
            }

            if (!expectHidden && !isVisible) {
                return ExecutionResult.failure(
                        "ASSERT_VISIBLE failed — element is not visible: ["
                                + step.getRecipient() + "]"
                );
            }

            return ExecutionResult.success();

        } catch (Exception ex) {
            return ExecutionResult.failure(
                    "ASSERT_VISIBLE failed: " + ex.getMessage()
            );
        }
    }
}
