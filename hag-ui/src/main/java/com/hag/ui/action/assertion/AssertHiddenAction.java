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
 * ASSERT_HIDDEN — asserts that an element is absent from the DOM or not displayed.
 *
 * <pre>
 *   ASSERT_HIDDEN,Page.Element,,
 * </pre>
 *
 * Passes when:
 * <ul>
 *   <li>The element is not found in the DOM at all, OR</li>
 *   <li>The element exists but {@code isDisplayed()} returns {@code false}</li>
 * </ul>
 */
public final class AssertHiddenAction implements UiAction {

    @Override
    public String name() { return "ASSERT_HIDDEN"; }

    @Override
    public ExecutionResult execute(Step step, ActionDescriptor descriptor, ExecutionContext context) {

        if (step.getRecipient() == null || step.getRecipient().isBlank()) {
            return ExecutionResult.failure("ASSERT_HIDDEN requires recipient (locator key)");
        }

        try {
            WebDriver driver = UiDriverExtractor.requireDriver(context.getUiAdapter());
            By by = LocatorResolver.resolve(step.getRecipient());
            List<WebElement> elements = driver.findElements(by);

            // Not in DOM → hidden ✓
            if (elements.isEmpty()) return ExecutionResult.success();

            // In DOM but not displayed → hidden ✓
            if (!elements.get(0).isDisplayed()) return ExecutionResult.success();

            return ExecutionResult.failure(
                    "ASSERT_HIDDEN failed — element is visible: [" + step.getRecipient() + "]"
            );

        } catch (Exception ex) {
            return ExecutionResult.failure("ASSERT_HIDDEN error: " + ex.getMessage());
        }
    }
}
