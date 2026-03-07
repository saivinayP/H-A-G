package com.hag.ui.action.click;

import com.hag.core.adapter.UiAdapter;
import com.hag.core.context.ExecutionContext;
import com.hag.core.dispatcher.descriptor.ActionDescriptor;
import com.hag.core.model.Step;
import com.hag.core.result.ExecutionResult;
import com.hag.ui.action.UiAction;
import com.hag.ui.adapter.SeleniumUiAdapter;
import com.hag.ui.locator.LocatorResolver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

import java.util.Objects;

public final class ClickAction implements UiAction {

    @Override
    public String name() {
        return "CLICK";
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
                    "CLICK requires recipient (locator key)"
            );
        }

        UiAdapter adapter = context.getUiAdapter();

        if (!(adapter instanceof SeleniumUiAdapter seleniumAdapter)) {
            return ExecutionResult.failure(
                    "SeleniumUiAdapter not configured in ExecutionContext"
            );
        }

        WebDriver driver = seleniumAdapter.driver();

        try {

            By by = LocatorResolver.resolve(step.getRecipient());

            WebElement element = driver.findElement(by);

            if (descriptor.hasFlag("double")) {

                new Actions(driver)
                        .doubleClick(element)
                        .perform();

            } else {

                element.click();
            }

            return ExecutionResult.success();

        } catch (Exception ex) {

            return ExecutionResult.failure(
                    "CLICK failed for locator [" +
                            step.getRecipient() +
                            "]: " + ex.getMessage()
            );
        }
    }
}