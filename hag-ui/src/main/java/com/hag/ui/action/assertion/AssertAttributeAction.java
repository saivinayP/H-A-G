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
import org.openqa.selenium.WebElement;

/**
 * ASSERT_ATTRIBUTE
 *
 * Asserts that a specific HTML attribute of an element holds an expected value.
 *
 * CSV usage:
 *   Action                      | Recipient    | Source         | Key
 *   ASSERT_ATTRIBUTE            | Page.Element | attributeName  | expectedValue
 *   ASSERT_ATTRIBUTE:contains   | Page.Element | class          | active
 *
 * Fields:
 *   Recipient = locator key
 *   Source    = attribute name (e.g. "class", "href", "value", "disabled")
 *   Key       = expected attribute value (or ${VAR} placeholder)
 *
 * Flags:
 *   contains — substring match instead of exact equality
 */
public final class AssertAttributeAction implements UiAction {

    @Override
    public String name() {
        return "ASSERT_ATTRIBUTE";
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
                    "ASSERT_ATTRIBUTE requires recipient (locator key)"
            );
        }

        if (step.getSource() == null
                || step.getSource().isBlank()) {
            return ExecutionResult.failure(
                    "ASSERT_ATTRIBUTE requires source field (attribute name)"
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
                    "ASSERT_ATTRIBUTE expected value resolved to null"
            );
        }

        try {

            WebDriver driver =
                    UiDriverExtractor.requireDriver(context.getUiAdapter());

            By by = LocatorResolver.resolve(step.getRecipient());
            WebElement element = driver.findElement(by);

            String attrName   = step.getSource().trim();
            String actualVal  = element.getAttribute(attrName);
            String expectedVal = resolvedValue.toString();

            if (actualVal == null) {
                return ExecutionResult.failure(
                        "ASSERT_ATTRIBUTE failed — attribute ["
                                + attrName + "] not found on element ["
                                + step.getRecipient() + "]"
                );
            }

            boolean passed = descriptor.hasFlag("contains")
                    ? actualVal.contains(expectedVal)
                    : actualVal.equals(expectedVal);

            if (!passed) {
                return ExecutionResult.failure(
                        "ASSERT_ATTRIBUTE failed for locator ["
                                + step.getRecipient()
                                + "], attribute [" + attrName
                                + "]. Expected"
                                + (descriptor.hasFlag("contains") ? " to contain" : "")
                                + ": [" + expectedVal
                                + "] but was: [" + actualVal + "]"
                );
            }

            return ExecutionResult.success();

        } catch (Exception ex) {
            return ExecutionResult.failure(
                    "ASSERT_ATTRIBUTE failed: " + ex.getMessage()
            );
        }
    }
}
