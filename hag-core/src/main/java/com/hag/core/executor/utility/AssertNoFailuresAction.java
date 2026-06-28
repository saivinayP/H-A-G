package com.hag.core.executor.utility;

import com.hag.core.context.ExecutionContext;
import com.hag.core.dispatcher.descriptor.ActionDescriptor;
import com.hag.core.executor.Action;
import com.hag.core.model.Step;
import com.hag.core.result.ExecutionResult;

import java.util.List;

public class AssertNoFailuresAction implements Action {

    @Override
    public String name() {
        return "ASSERT_NO_FAILURES";
    }

    @Override
    public com.hag.core.executor.ActionCategory category() {
        return com.hag.core.executor.ActionCategory.CORE;
    }

    @Override
    public ExecutionResult execute(Step step, ActionDescriptor descriptor, ExecutionContext context) {
        List<String> softFailures = context.getSoftFailures();
        
        if (softFailures != null && !softFailures.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            sb.append("Failing on ASSERT_NO_FAILURES due to previous soft failures:\n");
            for (String fail : softFailures) {
                sb.append(" - ").append(fail).append("\n");
            }
            context.getSoftFailures().clear(); // Reset after reading
            return ExecutionResult.failure(sb.toString().trim());
        }

        return ExecutionResult.success();
    }
}
