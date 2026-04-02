package com.hag.ui.action.js;
import com.hag.core.context.ExecutionContext;
import com.hag.core.dispatcher.descriptor.ActionDescriptor;
import com.hag.core.model.Step;
import com.hag.core.resolver.StepValueResolver;
import com.hag.core.result.ExecutionResult;
import com.hag.ui.action.UiAction;
import com.hag.ui.util.UiDriverExtractor;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;

/**
 * JS_EXECUTE
 *
 * Executes an arbitrary JavaScript snippet.
 * Optionally stores the script's return value into the DataStore.
 *
 * CSV usage:
 *   Action      | Recipient | Source | Key
 *   JS_EXECUTE  |           |        | window.scrollTo(0,0)
 *   JS_EXECUTE  |           |        | return document.title          ← return value captured
 *
 * Parameters:
 *   store-as=VAR_NAME  — stores the JS return value under this key in the DataStore
 *
 * Example with store:
 *   Action                         | Key
 *   JS_EXECUTE(store-as=pageTitle) | return document.title;
 */
public final class JsExecuteAction implements UiAction {

    @Override
    public String name() {
        return "JS_EXECUTE";
    }

    @Override
    public ExecutionResult execute(
            Step step,
            ActionDescriptor descriptor,
            ExecutionContext context
    ) {

        final Object resolvedScript;
        try {
            resolvedScript = StepValueResolver.resolveValue(step, context);
        } catch (Exception ex) {
            return ExecutionResult.failure(ex.getMessage());
        }

        if (resolvedScript == null
                || resolvedScript.toString().isBlank()) {
            return ExecutionResult.failure(
                    "JS_EXECUTE requires a JavaScript expression in key field"
            );
        }

        try {

            WebDriver driver =
                    UiDriverExtractor.requireDriver(context.getUiAdapter());

            Object result = ((JavascriptExecutor) driver)
                    .executeScript(resolvedScript.toString());

            // Optionally store the return value
            String storeAs = descriptor.getParameter("store-as");
            if (storeAs != null && !storeAs.isBlank()) {
                context.getDataStore().put(storeAs.trim(), result);
            }

            return ExecutionResult.success();

        } catch (Exception ex) {
            return ExecutionResult.failure(
                    "JS_EXECUTE failed: " + ex.getMessage()
            );
        }
    }
}
