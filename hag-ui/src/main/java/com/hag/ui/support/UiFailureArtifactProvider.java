package com.hag.ui.support;

import com.hag.core.context.ExecutionContext;
import com.hag.core.engine.FailureArtifactProvider;

import java.nio.file.Path;
import java.util.Optional;

public final class UiFailureArtifactProvider
        implements FailureArtifactProvider {

    @Override
    public Optional<Path> capture(
            String testName,
            int stepIndex,
            ExecutionContext context
    ) {

        if (context == null
                || context.getUiAdapter() == null
                || context.getConfig() == null) {

            return Optional.empty();
        }

        return ScreenshotSupport.capture(
                context.getUiAdapter(),
                context.getConfig(),
                testName,
                stepIndex
        );
    }
}