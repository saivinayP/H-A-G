package com.hag.core.engine;

import com.hag.core.engine.context.ExecutionContext;

import java.nio.file.Path;

/**
 * Contract for executing a test definition.
 *
 * Implementations must:
 *  - Not create ExecutionContext internally
 *  - Not perform adapter wiring
 *  - Guarantee finally execution
 */
public interface ExecutionEngine {

    void execute(
            String testName,
            Path testFile,
            ExecutionContext context
    );
}
