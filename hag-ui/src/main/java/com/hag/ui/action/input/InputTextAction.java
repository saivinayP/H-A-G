package com.hag.ui.action.input;

import com.hag.core.context.ExecutionContext;
import com.hag.core.dispatcher.descriptor.ActionDescriptor;
import com.hag.core.dispatcher.descriptor.ModifierSet;
import com.hag.core.model.Step;
import com.hag.core.result.ExecutionResult;
import com.hag.ui.action.UiAction;
import com.hag.ui.locator.LocatorResolver;
import com.hag.ui.util.UiDriverExtractor;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * INPUT action — type text, send keyboard keys, or upload files.
 *
 * <pre>
 *   INPUT,Page.field,,text            → type literal text
 *   INPUT,Page.field,clear,text       → clear then type
 *   INPUT,Page.field,testdata/f.json,block  → type value from data block
 *   INPUT:KEY,Page.field,,Enter       → send keyboard key
 *   INPUT:FILE,Page.fileInput,,/path  → set file upload input
 * </pre>
 */
public final class InputTextAction implements UiAction {

    @Override
    public String name() {
        return "INPUT";
    }

    @Override
    public ExecutionResult execute(Step step, ActionDescriptor descriptor, ExecutionContext context) {

        if (step.getRecipient() == null || step.getRecipient().isBlank()) {
            return ExecutionResult.failure("INPUT requires recipient (locator key)");
        }

        try {
            WebDriver driver = UiDriverExtractor.requireDriver(context.getUiAdapter());
            By by = LocatorResolver.resolve(step.getRecipient());
            WebElement element = driver.findElement(by);
            ModifierSet mods = step.getModifiers();

            // INPUT:KEY — send a special keyboard key
            if (descriptor.isSubCase("KEY")) {
                String keyName = step.getKey();
                if (keyName == null || keyName.isBlank()) {
                    return ExecutionResult.failure("INPUT:KEY requires key name in Key column (e.g. Enter, Tab)");
                }
                Keys key = resolveKey(keyName);
                if (key == null) {
                    return ExecutionResult.failure("Unrecognised key name: [" + keyName + "]");
                }
                element.sendKeys(key);
                return ExecutionResult.success();
            }

            // INPUT:FILE — set file upload path
            if (descriptor.isSubCase("FILE")) {
                String filePath = step.getKey();
                if (filePath == null || filePath.isBlank()) {
                    return ExecutionResult.failure("INPUT:FILE requires file path in Key column");
                }
                element.sendKeys(filePath);
                return ExecutionResult.success();
            }

            // Default — type text (optionally clear first)
            Object resolvedValue = context.resolveValue(step.getKey());
            if (resolvedValue == null) {
                return ExecutionResult.failure("INPUT resolved value is null");
            }

            boolean shouldClear = mods != null && mods.hasFlag("clear");
            if (shouldClear) {
                element.clear();
            }

            element.sendKeys(resolvedValue.toString());
            return ExecutionResult.success();

        } catch (Exception ex) {
            return ExecutionResult.failure(
                    "INPUT failed for locator [" + step.getRecipient() + "]: " + ex.getMessage()
            );
        }
    }

    /** Maps user-friendly key names to Selenium {@link Keys} enum values. */
    private Keys resolveKey(String name) {
        return switch (name.trim().toUpperCase()) {
            case "ENTER", "RETURN"    -> Keys.ENTER;
            case "TAB"                -> Keys.TAB;
            case "ESCAPE", "ESC"      -> Keys.ESCAPE;
            case "SPACE"              -> Keys.SPACE;
            case "BACKSPACE"          -> Keys.BACK_SPACE;
            case "DELETE"             -> Keys.DELETE;
            case "ARROWUP", "UP"      -> Keys.ARROW_UP;
            case "ARROWDOWN", "DOWN"  -> Keys.ARROW_DOWN;
            case "ARROWLEFT", "LEFT"  -> Keys.ARROW_LEFT;
            case "ARROWRIGHT", "RIGHT"-> Keys.ARROW_RIGHT;
            case "HOME"               -> Keys.HOME;
            case "END"                -> Keys.END;
            case "PAGEUP"             -> Keys.PAGE_UP;
            case "PAGEDOWN"           -> Keys.PAGE_DOWN;
            case "F1"  -> Keys.F1;  case "F2"  -> Keys.F2;
            case "F3"  -> Keys.F3;  case "F4"  -> Keys.F4;
            case "F5"  -> Keys.F5;  case "F6"  -> Keys.F6;
            case "F7"  -> Keys.F7;  case "F8"  -> Keys.F8;
            case "F9"  -> Keys.F9;  case "F10" -> Keys.F10;
            case "F11" -> Keys.F11; case "F12" -> Keys.F12;
            default -> null;
        };
    }
}