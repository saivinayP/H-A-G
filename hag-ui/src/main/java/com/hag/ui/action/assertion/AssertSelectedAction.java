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
 * ASSERT_SELECTED — asserts that a checkbox or radio button is in the selected/checked state.
 *
 * <pre>
 *   ASSERT_SELECTED,Page.darkModeToggle,,
 * </pre>
 */
public final class AssertSelectedAction implements UiAction {

    @Override
    public String name() { return "ASSERT_SELECTED"; }

    @Override
    public ExecutionResult execute(Step step, ActionDescriptor descriptor, ExecutionContext context) {

        if (step.getRecipient() == null || step.getRecipient().isBlank()) {
            return ExecutionResult.failure("ASSERT_SELECTED requires recipient (locator key)");
        }

        try {
            WebDriver driver = UiDriverExtractor.requireDriver(context.getUiAdapter());
            By by = LocatorResolver.resolve(step.getRecipient());
            WebElement element = driver.findElement(by);

            if (!element.isSelected()) {
                return ExecutionResult.failure(
                        "ASSERT_SELECTED failed — element is not selected: [" + step.getRecipient() + "]"
                );
            }
            return ExecutionResult.success();

        } catch (Exception ex) {
            return ExecutionResult.failure("ASSERT_SELECTED error: " + ex.getMessage());
        }
    }
}
