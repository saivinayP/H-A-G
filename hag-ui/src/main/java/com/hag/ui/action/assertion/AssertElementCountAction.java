package com.hag.ui.action.assertion;

import com.hag.core.context.ExecutionContext;
import com.hag.core.dispatcher.descriptor.ActionDescriptor;
import com.hag.core.model.Step;
import com.hag.core.resolver.StepValueResolver;
import com.hag.core.result.ExecutionResult;
import com.hag.ui.action.UiAction;
import com.hag.ui.locator.LocatorResolver;
import com.hag.ui.util.UiDriverExtractor;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import java.util.List;

/**
 * ASSERT_COUNT
 *
 * Asserts that the number of elements matching a locator equals the expected count.
 *
 * CSV usage:
 *   Action        | Recipient    | Source | Key
 *   ASSERT_COUNT  | Page.Element |        | 5
 *
 * Key = expected integer count (or ${VAR} placeholder).
 */
public final class AssertElementCountAction implements UiAction {

    @Override
    public String name() {
        return "ASSERT_COUNT";
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
                    "ASSERT_COUNT requires recipient (locator key)"
            );
        }

        final Object resolvedValue;
        try {
            resolvedValue = StepValueResolver.resolveValue(step, context);
        } catch (Exception ex) {
            return ExecutionResult.failure(ex.getMessage());
        }

        if (resolvedValue == null) {
            return ExecutionResult.failure(
                    "ASSERT_COUNT expected count resolved to null"
            );
        }

        final int expectedCount;
        try {
            expectedCount = Integer.parseInt(resolvedValue.toString().trim());
        } catch (NumberFormatException ex) {
            return ExecutionResult.failure(
                    "ASSERT_COUNT expected value must be an integer, got: "
                            + resolvedValue
            );
        }

        try {

            WebDriver driver =
                    UiDriverExtractor.requireDriver(context.getUiAdapter());

            By by = LocatorResolver.resolve(step.getRecipient());
            List<?> elements = driver.findElements(by);
            int actualCount = elements.size();

            if (actualCount != expectedCount) {
                return ExecutionResult.failure(
                        "ASSERT_COUNT failed for locator ["
                                + step.getRecipient()
                                + "]. Expected: " + expectedCount
                                + " but found: " + actualCount
                );
            }

            return ExecutionResult.success();

        } catch (Exception ex) {
            return ExecutionResult.failure(
                    "ASSERT_COUNT failed: " + ex.getMessage()
            );
        }
    }
}
