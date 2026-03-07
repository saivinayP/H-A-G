package com.hag.core.resolver;

import com.hag.core.context.ExecutionContext;
import com.hag.core.model.Step;

public final class StepValueResolver {

    private StepValueResolver() {}

    public static Object resolveValue(
            Step step,
            ExecutionContext context
    ) {

        if (step.getSource() != null
                && !step.getSource().isBlank()) {

            if (context.getTestDataResolver() == null) {
                throw new IllegalStateException(
                        "TestDataResolver not configured"
                );
            }

            return context.getTestDataResolver()
                    .resolve(step.getSource(), step.getKey());
        }

        return context.resolveValue(step.getKey());
    }
}