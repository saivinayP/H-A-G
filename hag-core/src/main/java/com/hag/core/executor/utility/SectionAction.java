package com.hag.core.executor.utility;

import com.hag.core.context.ExecutionContext;
import com.hag.core.dispatcher.descriptor.ActionDescriptor;
import com.hag.core.executor.Action;
import com.hag.core.model.Step;
import com.hag.core.result.ExecutionResult;

public class SectionAction implements Action {

    @Override
    public String name() {
        return "SECTION";
    }

    @Override
    public com.hag.core.executor.ActionCategory category() {
        return com.hag.core.executor.ActionCategory.CORE;
    }

    @Override
    public ExecutionResult execute(Step step, ActionDescriptor descriptor, ExecutionContext context) {
        // Functionally inert runtime action. Used as a marker by the reporting engine.
        // It does not alter test state.
        return ExecutionResult.success();
    }
}
