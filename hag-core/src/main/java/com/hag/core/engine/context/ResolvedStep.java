package com.hag.core.engine.context;

import com.hag.core.engine.model.Step;

public class ResolvedStep {

    private ResolvedStep(){}

    public static Step resolve(Step original, ExecutionContext context){

        String resolvedRecipient = ValueInterpolator.interpolate(original.getRecipient(), context);
        String resolvedKey = ValueInterpolator.interpolate(original.getKey(), context);
        String resolvedSource = ValueInterpolator.interpolate(original.getSource(), context);

        return new Step(
                original.getAction(),
                resolvedRecipient,
                resolvedSource,
                resolvedKey,
                original.getRawLine()
        );
    }
}
