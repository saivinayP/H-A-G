package com.hag.ui.action.navigation;

import com.hag.core.context.ExecutionContext;
import com.hag.core.dispatcher.descriptor.ActionDescriptor;
import com.hag.core.model.Step;
import com.hag.core.result.ExecutionResult;
import com.hag.core.resolver.StepValueResolver;
import com.hag.ui.action.UiAction;
import com.hag.ui.util.UiDriverExtractor;
import org.openqa.selenium.WebDriver;

public final class NavigateAction implements UiAction {

    @Override
    public String name() {
        return "NAVIGATE";
    }

    @Override
    public ExecutionResult execute(
            Step step,
            ActionDescriptor descriptor,
            ExecutionContext context
    ) {

        // NAVIGATE should not use recipient
        if (step.getKey() == null
                || step.getKey().isBlank()) {

            return ExecutionResult.failure(
                    "NAVIGATE requires URL in key field"
            );
        }

        final String resolvedUrl;

        try {
            Object value =
                    StepValueResolver.resolveValue(step, context);

            if (value == null) {
                return ExecutionResult.failure(
                        "NAVIGATE resolved URL is null"
                );
            }

            resolvedUrl = value.toString();

        } catch (Exception ex) {

            return ExecutionResult.failure(
                    ex.getMessage()
            );
        }

        try {

            WebDriver driver =
                    UiDriverExtractor.requireDriver(
                            context.getUiAdapter()
                    );

            String finalUrl =
                    buildFinalUrl(resolvedUrl, context);

            driver.get(finalUrl);

            return ExecutionResult.success();

        } catch (Exception ex) {

            return ExecutionResult.failure(
                    "NAVIGATE failed for URL [" +
                            resolvedUrl +
                            "]: " + ex.getMessage()
            );
        }
    }

    private String buildFinalUrl(
            String resolvedUrl,
            ExecutionContext context
    ) {

        String baseUrl =
                context.getConfig().getBaseUrl();

        if (baseUrl == null || baseUrl.isBlank()) {
            return resolvedUrl;
        }

        // Absolute URL provided → ignore baseUrl
        if (resolvedUrl.startsWith("http://")
                || resolvedUrl.startsWith("https://")) {

            return resolvedUrl;
        }

        if (baseUrl.endsWith("/")
                && resolvedUrl.startsWith("/")) {

            return baseUrl + resolvedUrl.substring(1);
        }

        if (!baseUrl.endsWith("/")
                && !resolvedUrl.startsWith("/")) {

            return baseUrl + "/" + resolvedUrl;
        }

        return baseUrl + resolvedUrl;
    }
}