package com.hag.ui.action.input;

import com.hag.core.context.ExecutionContext;
import com.hag.core.dispatcher.descriptor.ActionDescriptor;
import com.hag.core.model.Step;
import com.hag.core.result.ExecutionResult;
import com.hag.core.resolver.StepValueResolver;
import com.hag.ui.action.UiAction;
import com.hag.ui.util.UiDriverExtractor;
import com.hag.ui.locator.LocatorResolver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public final class InputTextAction implements UiAction {

    @Override
    public String name() {
        return "INPUT";
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
                    "INPUT requires recipient (locator key)"
            );
        }

        Object resolvedValue;

        try {
            resolvedValue =
                    StepValueResolver.resolveValue(step, context);
        } catch (Exception ex) {
            return ExecutionResult.failure(ex.getMessage());
        }

        if (resolvedValue == null) {
            return ExecutionResult.failure(
                    "INPUT resolved value is null"
            );
        }

        try {

            WebDriver driver =
                    UiDriverExtractor.requireDriver(
                            context.getUiAdapter()
                    );

            By by =
                    LocatorResolver.resolve(step.getRecipient());

            WebElement element =
                    driver.findElement(by);

            if (descriptor.hasFlag("clear")) {
                element.clear();
            }

            element.sendKeys(resolvedValue.toString());

            return ExecutionResult.success();

        } catch (Exception ex) {

            return ExecutionResult.failure(
                    "INPUT failed for locator [" +
                            step.getRecipient() +
                            "]: " + ex.getMessage()
            );
        }
    }
}