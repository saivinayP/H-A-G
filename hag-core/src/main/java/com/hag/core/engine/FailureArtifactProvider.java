package com.hag.core.engine;

import com.hag.core.context.ExecutionContext;

import java.nio.file.Path;
import java.util.Optional;

/**
 * Provides failure artifacts (e.g., screenshot).
 * Implemented by UI module.
 */
public interface FailureArtifactProvider {

    Optional<Path> capture(
            String testName,
            int stepIndex,
            ExecutionContext context
    );
}