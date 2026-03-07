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
 * ASSERT_DISABLED — asserts that an element has the {@code disabled} attribute
 * (i.e. {@link WebElement#isEnabled()} returns {@code false}).
 *
 * <pre>
 *   ASSERT_DISABLED,Page.submitBtn,,
 * </pre>
 */
public final class AssertDisabledAction implements UiAction {

    @Override
    public String name() { return "ASSERT_DISABLED"; }

    @Override
    public ExecutionResult execute(Step step, ActionDescriptor descriptor, ExecutionContext context) {

        if (step.getRecipient() == null || step.getRecipient().isBlank()) {
            return ExecutionResult.failure("ASSERT_DISABLED requires recipient (locator key)");
        }

        try {
            WebDriver driver = UiDriverExtractor.requireDriver(context.getUiAdapter());
            By by = LocatorResolver.resolve(step.getRecipient());
            WebElement element = driver.findElement(by);

            if (element.isEnabled()) {
                return ExecutionResult.failure(
                        "ASSERT_DISABLED failed — element is enabled: [" + step.getRecipient() + "]"
                );
            }
            return ExecutionResult.success();

        } catch (Exception ex) {
            return ExecutionResult.failure("ASSERT_DISABLED error: " + ex.getMessage());
        }
    }
}
