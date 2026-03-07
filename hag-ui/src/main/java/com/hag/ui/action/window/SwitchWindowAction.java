package com.hag.ui.action.window;

import com.hag.core.context.ExecutionContext;
import com.hag.core.dispatcher.descriptor.ActionDescriptor;
import com.hag.core.model.Step;
import com.hag.core.result.ExecutionResult;
import com.hag.ui.action.UiAction;
import com.hag.ui.util.UiDriverExtractor;
import org.openqa.selenium.WebDriver;

import java.util.ArrayList;
import java.util.List;

/**
 * SWITCH_WINDOW
 *
 * Switches the WebDriver's focus to another browser window or tab.
 *
 * CSV usage:
 *   Action                            | Recipient | Source | Key
 *   SWITCH_WINDOW                     |           |        | new        ← latest opened window
 *   SWITCH_WINDOW                     |           |        | My Title   ← by page title
 *   SWITCH_WINDOW:close-current       |           |        | new        ← close current, then switch
 *
 * Key values:
 *   "new"        — switches to the most recently opened window/tab
 *   any string   — switches to the window whose title matches the value
 *
 * Flag:
 *   close-current — closes the currently focused window/tab before switching
 */
public final class SwitchWindowAction implements UiAction {

    @Override
    public String name() {
        return "SWITCH_WINDOW";
    }

    @Override
    public ExecutionResult execute(
            Step step,
            ActionDescriptor descriptor,
            ExecutionContext context
    ) {

        if (step.getKey() == null || step.getKey().isBlank()) {
            return ExecutionResult.failure(
                    "SWITCH_WINDOW requires key ('new' or page title)"
            );
        }

        try {

            WebDriver driver =
                    UiDriverExtractor.requireDriver(context.getUiAdapter());

            String currentHandle = driver.getWindowHandle();

            if (descriptor.hasFlag("close-current")) {
                driver.close();
            }

            List<String> handles =
                    new ArrayList<>(driver.getWindowHandles());

            if (handles.isEmpty()) {
                return ExecutionResult.failure(
                        "SWITCH_WINDOW failed — no open windows found"
                );
            }

            String target = step.getKey().trim();

            if ("new".equalsIgnoreCase(target)) {
                // Switch to the last (most recently opened) handle
                String lastHandle = handles.get(handles.size() - 1);
                driver.switchTo().window(lastHandle);
                return ExecutionResult.success();
            }

            // Switch by page title
            for (String handle : handles) {
                if (handle.equals(currentHandle)
                        && !descriptor.hasFlag("close-current")) {
                    continue;
                }
                driver.switchTo().window(handle);
                if (driver.getTitle().equals(target)) {
                    return ExecutionResult.success();
                }
            }

            return ExecutionResult.failure(
                    "SWITCH_WINDOW failed — no window with title: [" + target + "]"
            );

        } catch (Exception ex) {
            return ExecutionResult.failure(
                    "SWITCH_WINDOW failed: " + ex.getMessage()
            );
        }
    }
}
