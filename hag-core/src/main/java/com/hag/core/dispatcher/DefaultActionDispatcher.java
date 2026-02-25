package com.hag.core.dispatcher;

import com.hag.core.context.ExecutionContext;
import com.hag.core.dispatcher.descriptor.ActionDescriptor;
import com.hag.core.dispatcher.descriptor.ActionDescriptorParser;
import com.hag.core.executor.Action;
import com.hag.core.model.Step;
import com.hag.core.result.ExecutionResult;

public final class DefaultActionDispatcher
        implements ActionDispatcher {

    private final ActionRegistry registry;
    private final ActionDescriptorParser parser =
            new ActionDescriptorParser();

    public DefaultActionDispatcher(ActionRegistry registry) {
        this.registry = registry;
    }

    @Override
    public ExecutionResult dispatch(
            Step step,
            ExecutionContext context
    ) {

        ActionDescriptor descriptor =
                parser.parse(step.getAction());

        Action action =
                registry.resolve(descriptor.name())
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "No action registered: "
                                                + descriptor.name()
                                )
                        );

        return action.execute(step, context);
    }
}