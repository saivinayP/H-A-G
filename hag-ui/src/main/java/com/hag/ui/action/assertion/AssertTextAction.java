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
 * ASSERT_TEXT
 *
 * Asserts that an element's visible text matches the expected value.
 *
 * CSV usage:
 *   Action                      | Recipient    | Source | Key
 *   ASSERT_TEXT                 | Page.Element |        | exact text
 *   ASSERT_TEXT:contains        | Page.Element |        | partial text
 *   ASSERT_TEXT:ignore-case     | Page.Element |        | Text (case-insensitive)
 *
 * Flags:
 *   contains     — substring match (actual contains expected)
 *   ignore-case  — case-insensitive comparison (can combine with contains)
 */
public final class AssertTextAction implements UiAction {

    @Override
    public String name() {
        return "ASSERT_TEXT";
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
                    "ASSERT_TEXT requires recipient (locator key)"
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
                    "ASSERT_TEXT expected value resolved to null"
            );
        }

        try {

            WebDriver driver =
                    UiDriverExtractor.requireDriver(context.getUiAdapter());

            By by = LocatorResolver.resolve(step.getRecipient());
            WebElement element = driver.findElement(by);
            String actualText = element.getText();

            String expected = resolvedValue.toString();

            boolean ignoreCase = descriptor.hasFlag("ignore-case");
            boolean contains   = descriptor.hasFlag("contains");

            String actual = ignoreCase
                    ? actualText.toLowerCase()
                    : actualText;
            String exp = ignoreCase
                    ? expected.toLowerCase()
                    : expected;

            boolean passed = contains
                    ? actual.contains(exp)
                    : actual.equals(exp);

            if (!passed) {
                return ExecutionResult.failure(
                        "ASSERT_TEXT failed for locator ["
                                + step.getRecipient()
                                + "]. Expected"
                                + (contains ? " to contain" : "") + ": ["
                                + expected + "] but was: [" + actualText + "]"
                );
            }

            return ExecutionResult.success();

        } catch (Exception ex) {
            return ExecutionResult.failure(
                    "ASSERT_TEXT failed: " + ex.getMessage()
            );
        }
    }
}
