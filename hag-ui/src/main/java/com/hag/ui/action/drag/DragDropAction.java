package com.hag.ui.action.drag;

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
import org.openqa.selenium.interactions.Actions;

/**
 * DRAG_DROP
 *
 * Performs a drag-and-drop operation from a source element to a target element.
 *
 * CSV usage:
 *   Action    | Recipient         | Source | Key
 *   DRAG_DROP | Page.SourceElem   |        | Page.TargetElem
 *
 * Fields:
 *   Recipient = source element locator key (element to drag from)
 *   Key       = target element locator key (element to drop onto)
 */
public final class DragDropAction implements UiAction {

    @Override
    public String name() {
        return "DRAG_DROP";
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
                    "DRAG_DROP requires recipient (source locator key)"
            );
        }

        if (step.getKey() == null || step.getKey().isBlank()) {
            return ExecutionResult.failure(
                    "DRAG_DROP requires key (target locator key)"
            );
        }

        final Object resolvedTarget;
        try {
            resolvedTarget = StepValueResolver.resolveValue(step, context);
        } catch (Exception ex) {
            return ExecutionResult.failure(ex.getMessage());
        }

        if (resolvedTarget == null) {
            return ExecutionResult.failure(
                    "DRAG_DROP target locator resolved to null"
            );
        }

        try {

            WebDriver driver =
                    UiDriverExtractor.requireDriver(context.getUiAdapter());

            By sourceBy = LocatorResolver.resolve(step.getRecipient());
            By targetBy = LocatorResolver.resolve(resolvedTarget.toString());

            WebElement source = driver.findElement(sourceBy);
            WebElement target = driver.findElement(targetBy);

            new Actions(driver)
                    .dragAndDrop(source, target)
                    .perform();

            return ExecutionResult.success();

        } catch (Exception ex) {
            return ExecutionResult.failure(
                    "DRAG_DROP failed from ["
                            + step.getRecipient()
                            + "] to [" + step.getKey()
                            + "]: " + ex.getMessage()
            );
        }
    }
}
