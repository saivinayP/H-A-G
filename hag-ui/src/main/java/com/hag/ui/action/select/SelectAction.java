package com.hag.ui.action.select;

import com.hag.core.context.ExecutionContext;
import com.hag.core.dispatcher.descriptor.ActionDescriptor;
import com.hag.core.model.Step;
import com.hag.core.result.ExecutionResult;
import com.hag.core.resolver.StepValueResolver;
import com.hag.ui.action.UiAction;
import com.hag.ui.locator.LocatorResolver;
import com.hag.ui.util.UiDriverExtractor;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

/**
 * SELECT
 *
 * Selects an option in a &lt;select&gt; dropdown.
 *
 * CSV usage:
 *   Action       | Recipient      | Source | Key
 *   SELECT       | Page.Element   |        | optionValue
 *   SELECT:text  | Page.Element   |        | Visible Text
 *   SELECT:index | Page.Element   |        | 2
 *
 * Flags:
 *   text  — select by visible text
 *   index — select by integer index (0-based)
 *   (none) — select by value attribute (default)
 */
public final class SelectAction implements UiAction {

    @Override
    public String name() {
        return "SELECT";
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
                    "SELECT requires recipient (locator key)"
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
                    "SELECT resolved value is null"
            );
        }

        try {

            WebDriver driver =
                    UiDriverExtractor.requireDriver(context.getUiAdapter());

            By by = LocatorResolver.resolve(step.getRecipient());

            WebElement element = driver.findElement(by);
            Select select = new Select(element);

            if (descriptor.hasFlag("text")) {
                select.selectByVisibleText(resolvedValue.toString());

            } else if (descriptor.hasFlag("index")) {
                try {
                    int index = Integer.parseInt(resolvedValue.toString().trim());
                    select.selectByIndex(index);
                } catch (NumberFormatException ex) {
                    return ExecutionResult.failure(
                            "SELECT:index requires an integer value, got: "
                                    + resolvedValue
                    );
                }

            } else {
                select.selectByValue(resolvedValue.toString());
            }

            return ExecutionResult.success();

        } catch (Exception ex) {
            return ExecutionResult.failure(
                    "SELECT failed for locator ["
                            + step.getRecipient()
                            + "]: " + ex.getMessage()
            );
        }
    }
}
