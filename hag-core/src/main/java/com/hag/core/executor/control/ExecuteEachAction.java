package com.hag.core.executor.control;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hag.core.context.ExecutionContext;
import com.hag.core.dispatcher.descriptor.ActionDescriptor;
import com.hag.core.executor.Action;
import com.hag.core.model.Step;
import com.hag.core.result.ExecutionResult;

import java.util.Arrays;
import java.util.List;

/**
 * Loops over a collection and executes a subscript for each item.
 * Action: EXECUTE:EACH
 * Target: [item1, item2] or item1,item2
 * Value: subscript.csv | loopVarName
 */
public class ExecuteEachAction implements Action {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public String name() {
        return "EXECUTE";
    }

    @Override
    public com.hag.core.executor.ActionCategory category() {
        return com.hag.core.executor.ActionCategory.CORE;
    }

    @Override
    public ExecutionResult execute(Step step, ActionDescriptor descriptor, ExecutionContext context) {
        if (!"EACH".equalsIgnoreCase(descriptor.subCase())) {
            return ExecutionResult.skipped();
        }
        String iterable = step.getRecipient(); // e.g. [1, 2, 3] or comma separated
        String scriptParams = step.getSource(); // subscript.csv | loopVar

        if (iterable == null || iterable.isBlank()) {
            return ExecutionResult.failure("EXECUTE:EACH requires a list in the Target column.");
        }
        if (scriptParams == null || scriptParams.isBlank()) {
            return ExecutionResult.failure("EXECUTE:EACH requires a script path in the Value column.");
        }

        String subscript = scriptParams;
        String loopVar = "item";

        if (scriptParams.contains("|")) {
            String[] parts = scriptParams.split("\\|", 2);
            subscript = parts[0].trim();
            loopVar = parts[1].trim();
        }

        List<Object> items;
        try {
            if (iterable.trim().startsWith("[")) {
                items = MAPPER.readValue(iterable, new TypeReference<List<Object>>() {});
            } else {
                items = Arrays.asList((Object[]) iterable.split(","));
            }
        } catch (Exception e) {
            return ExecutionResult.failure("Failed to parse iterable collection: " + e.getMessage());
        }

        if (context.getEngine() == null) {
            return ExecutionResult.failure("ExecutionEngine is not available in the context.");
        }

        for (int i = 0; i < items.size(); i++) {
            Object item = items.get(i);
            
            // Set loop variables
            context.getDataStore().put(loopVar, item);
            context.getDataStore().put(loopVar + "_INDEX", i);
            context.getDataStore().put(loopVar + "_1_INDEX", i + 1);

            ExecutionResult result = context.getEngine().runSubscript("Subscript-" + subscript + "-Loop" + i, subscript, context);
            if (result.isFailure()) {
                return result; // Fast fail if a loop iteration fails
            }
        }

        return ExecutionResult.success();
    }
}
