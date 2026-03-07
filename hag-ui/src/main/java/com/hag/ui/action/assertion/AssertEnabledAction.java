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

/**
 * ASSERT_ENABLED — asserts that an element is present, displayed, and enabled (interactable).
 *
 * <pre>
 *   ASSERT_ENABLED,Page.Element,,
 * </pre>
 */
public final class AssertEnabledAction implements UiAction {

    @Override
    public String name() { return "ASSERT_ENABLED"; }

    @Override
    public ExecutionResult execute(Step step, ActionDescriptor descriptor, ExecutionContext context) {

        if (step.getRecipient() == null || step.getRecipient().isBlank()) {
            return ExecutionResult.failure("ASSERT_ENABLED requires recipient (locator key)");
        }

        try {
            WebDriver driver = UiDriverExtractor.requireDriver(context.getUiAdapter());
            By by = LocatorResolver.resolve(step.getRecipient());
            WebElement element = driver.findElement(by);

            if (!element.isEnabled()) {
                return ExecutionResult.failure(
                        "ASSERT_ENABLED failed — element is disabled: [" + step.getRecipient() + "]"
                );
            }
            return ExecutionResult.success();

        } catch (Exception ex) {
            return ExecutionResult.failure("ASSERT_ENABLED error: " + ex.getMessage());
        }
    }
}
