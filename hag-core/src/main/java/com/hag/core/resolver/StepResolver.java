package com.hag.core.resolver;

import com.hag.core.context.ExecutionContext;
import com.hag.core.context.ValueInterpolator;
import com.hag.core.model.Step;

public final class StepResolver {

    private StepResolver() {}

    public static Step resolve(
            Step original,
            ExecutionContext context
    ) {

        String resolvedRecipient =
                ValueInterpolator.interpolate(
                        original.getRecipient(),
                        context
                );

        String resolvedKey =
                ValueInterpolator.interpolate(
                        original.getKey(),
                        context
                );

        String resolvedSource =
                ValueInterpolator.interpolate(
                        original.getSource(),
                        context
                );

        return new Step(
                original.getAction(),
                resolvedRecipient,
                resolvedSource,
                resolvedKey,
                original.getRawLine()
        );
    }
}