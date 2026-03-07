package com.hag.ui.action.navigation;

import com.hag.core.context.ExecutionContext;
import com.hag.core.dispatcher.descriptor.ActionDescriptor;
import com.hag.core.model.Step;
import com.hag.core.result.ExecutionResult;
import com.hag.ui.action.UiAction;
import com.hag.ui.util.UiDriverExtractor;
import org.openqa.selenium.WebDriver;

/**
 * NAVIGATE action — open a URL or perform browser navigation commands.
 *
 * <pre>
 *   NAVIGATE,,,https://app.example.com/login   → open absolute URL
 *   NAVIGATE,,,/dashboard                      → open relative URL (prepended with baseUrl)
 *   NAVIGATE:BACK,,,                           → browser back
 *   NAVIGATE:FORWARD,,,                        → browser forward
 *   NAVIGATE:REFRESH,,,                        → reload current page
 * </pre>
 */
public final class NavigateAction implements UiAction {

    @Override
    public String name() {
        return "NAVIGATE";
    }

    @Override
    public ExecutionResult execute(Step step, ActionDescriptor descriptor, ExecutionContext context) {

        try {
            WebDriver driver = UiDriverExtractor.requireDriver(context.getUiAdapter());

            // Browser history / refresh sub-cases — no URL needed
            if (descriptor.isSubCase("BACK")) {
                driver.navigate().back();
                return ExecutionResult.success();
            }

            if (descriptor.isSubCase("FORWARD")) {
                driver.navigate().forward();
                return ExecutionResult.success();
            }

            if (descriptor.isSubCase("REFRESH")) {
                driver.navigate().refresh();
                return ExecutionResult.success();
            }

            // Default — open a URL from Key column
            String rawUrl = step.getKey();
            if (rawUrl == null || rawUrl.isBlank()) {
                return ExecutionResult.failure("NAVIGATE requires URL in Key column");
            }

            Object resolved = context.resolveValue(rawUrl);
            if (resolved == null) {
                return ExecutionResult.failure("NAVIGATE resolved URL is null");
            }

            String finalUrl = buildFinalUrl(resolved.toString(), context);
            driver.get(finalUrl);
            return ExecutionResult.success();

        } catch (Exception ex) {
            return ExecutionResult.failure("NAVIGATE failed: " + ex.getMessage());
        }
    }

    private String buildFinalUrl(String resolvedUrl, ExecutionContext context) {
        String baseUrl = context.getConfig().getBaseUrl();

        if (baseUrl == null || baseUrl.isBlank()) return resolvedUrl;

        if (resolvedUrl.startsWith("http://") || resolvedUrl.startsWith("https://")) {
            return resolvedUrl;
        }

        if (baseUrl.endsWith("/") && resolvedUrl.startsWith("/")) {
            return baseUrl + resolvedUrl.substring(1);
        }

        if (!baseUrl.endsWith("/") && !resolvedUrl.startsWith("/")) {
            return baseUrl + "/" + resolvedUrl;
        }

        return baseUrl + resolvedUrl;
    }
}