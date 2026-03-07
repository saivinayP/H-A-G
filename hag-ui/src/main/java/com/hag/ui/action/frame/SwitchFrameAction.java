package com.hag.ui.action.frame;

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
 * SWITCH_FRAME
 *
 * Switches the WebDriver's focus to an iframe/frame,
 * or returns to the main document.
 *
 * CSV usage:
 *   Action        | Recipient    | Source | Key
 *   SWITCH_FRAME  | Page.Element |        |       ← switch into iframe
 *   SWITCH_FRAME  | DEFAULT      |        |       ← return to main document
 *
 * Recipient:
 *   "DEFAULT" (case-insensitive) — switches back to main document content
 *   Any other value             — treated as a locator key for the frame element
 */
public final class SwitchFrameAction implements UiAction {

    @Override
    public String name() {
        return "SWITCH_FRAME";
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
                    "SWITCH_FRAME requires recipient (locator key or 'DEFAULT')"
            );
        }

        try {

            WebDriver driver =
                    UiDriverExtractor.requireDriver(context.getUiAdapter());

            if ("DEFAULT".equalsIgnoreCase(step.getRecipient().trim())) {
                driver.switchTo().defaultContent();
                return ExecutionResult.success();
            }

            By by = LocatorResolver.resolve(step.getRecipient());
            WebElement frame = driver.findElement(by);
            driver.switchTo().frame(frame);

            return ExecutionResult.success();

        } catch (Exception ex) {
            return ExecutionResult.failure(
                    "SWITCH_FRAME failed for ["
                            + step.getRecipient()
                            + "]: " + ex.getMessage()
            );
        }
    }
}
