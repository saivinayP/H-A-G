package com.hag.core.engine.executor.api;

import com.hag.core.engine.adapter.ApiAdapter;
import com.hag.core.engine.adapter.ApiResponse;
import com.hag.core.engine.context.ExecutionContext;
import com.hag.core.engine.executor.StepExecutor;
import com.hag.core.engine.model.Step;
import com.hag.core.engine.resolver.TemplateResolver;
import com.hag.core.engine.result.ExecutionResult;
import com.hag.core.engine.result.api.ApiExecutionResult;
import com.hag.core.engine.result.EmptyExecutionResult;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class SendRequestExecutor implements StepExecutor {

    @Override
    public ExecutionResult execute(
            Step step,
            ExecutionContext context
    ) {

        try {
            // 1. Load template
            String template =
                    Files.readString(Path.of(step.getRecipient()));

            // 2. Resolve test data
            Object dataBlock =
                    context.getTestDataResolver()
                            .resolve(step.getSource(), step.getKey());

            Map<String, Object> testData =
                    dataBlock instanceof Map
                            ? (Map<String, Object>) dataBlock
                            : Map.of();

            // 3. Substitute variables
            String payload =
                    TemplateResolver.resolve(
                            template,
                            testData,
                            context
                    );

            // 4. Execute request
            ApiAdapter adapter = context.getApiAdapter();

            ApiResponse response =
                    adapter.send(
                            payload,
                            Map.of(),
                            step.getRecipient()
                    );

            // 5. Return result
            return new ApiExecutionResult(
                    Map.of(
                            ApiExecutionResult.RESPONSE, response.getBody(),
                            ApiExecutionResult.STATUS_CODE, response.getStatusCode(),
                            ApiExecutionResult.HEADERS, response.getHeaders()
                    )
            );

        } catch (Exception e) {
            throw new IllegalStateException(
                    "API execution failed for template: " +
                            step.getRecipient(),
                    e
            );
        }
    }
}
