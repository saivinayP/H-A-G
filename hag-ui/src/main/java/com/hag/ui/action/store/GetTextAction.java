package com.hag.ui.action.store;
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
 * GET_TEXT
 *
 * Reads the visible text of an element and stores it in the DataStore
 * so it can be referenced in later steps via ${VAR} or ${SCOPE:VAR}.
 *
 * CSV usage:
 *   Action    | Recipient    | Source | Key
 *   GET_TEXT  | Page.Element |        | myVariableName
 *
 * Fields:
 *   Recipient = locator key of the element to read
 *   Key       = variable name to store the text under
 *
 * Parameters:
 *   scope=GLOBAL|TEST|STEP — DataStore scope to use (default: TEST)
 *
 * Example:
 *   GET_TEXT | HomePage.WelcomeMsg | | capturedText
 *   → stores element.getText() as "capturedText"
 *   → reference later as ${capturedText}
 */
public final class GetTextAction implements UiAction {

    @Override
    public String name() {
        return "GET_TEXT";
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
                    "GET_TEXT requires recipient (locator key)"
            );
        }

        if (step.getKey() == null || step.getKey().isBlank()) {
            return ExecutionResult.failure(
                    "GET_TEXT requires key (variable name to store text)"
            );
        }

        try {

            WebDriver driver =
                    UiDriverExtractor.requireDriver(context.getUiAdapter());

            By by = LocatorResolver.resolve(step.getRecipient());
            WebElement element = driver.findElement(by);
            String text = element.getText();

            context.getDataStore().put(step.getKey().trim(), text);

            return ExecutionResult.success();

        } catch (Exception ex) {
            return ExecutionResult.failure(
                    "GET_TEXT failed for locator ["
                            + step.getRecipient()
                            + "]: " + ex.getMessage()
            );
        }
    }
}
