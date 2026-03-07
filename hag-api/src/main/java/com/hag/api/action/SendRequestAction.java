package com.hag.api.action;

import com.hag.api.adapter.RestAssuredApiAdapter;
import com.hag.api.model.ApiRequest;
import com.hag.api.model.ApiResponse;
import com.hag.api.template.TemplateMerger;
import com.hag.core.context.DataScope;
import com.hag.core.context.ExecutionContext;
import com.hag.core.dispatcher.descriptor.ActionDescriptor;
import com.hag.core.dispatcher.descriptor.ModifierSet;
import com.hag.core.executor.Action;
import com.hag.core.executor.ActionCategory;
import com.hag.core.model.Step;
import com.hag.core.result.ExecutionResult;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * SEND_REQUEST action — sends a REST HTTP request from a JSON template.
 *
 * <h3>CSV Syntax</h3>
 * <pre>
 *   SEND_REQUEST,templates/auth/login.json,,
 *   SEND_REQUEST,templates/auth/login.json,testdata/login/users.json,validUser
 * </pre>
 *
 * <ul>
 *   <li><b>Recipient</b> — path to the JSON request template (relative to templates root)</li>
 *   <li><b>Source</b>    — optional test-data JSON file path</li>
 *   <li><b>Key</b>       — optional data block name inside the test-data file</li>
 * </ul>
 *
 * <p>After execution the {@link ApiResponse} is stored in the context so that
 * assertion and store actions can access it via {@code context.getLastResult()}.
 */
public final class SendRequestAction implements Action {

    /** Context key under which the last API response is stored. */
    public static final String LAST_RESPONSE_KEY = "__api_last_response";

    @Override
    public String name() { return "SEND_REQUEST"; }

    @Override
    public ActionCategory category() { return ActionCategory.API; }

    @Override
    public ExecutionResult execute(Step step, ActionDescriptor descriptor, ExecutionContext context) {

        String templatePath = step.getRecipient();
        if (templatePath == null || templatePath.isBlank()) {
            return ExecutionResult.failure("SEND_REQUEST requires template path in Recipient column");
        }

        if (!context.hasApiAdapter()) {
            return ExecutionResult.failure("SEND_REQUEST requires an ApiAdapter in ExecutionContext");
        }

        if (!(context.getApiAdapter() instanceof RestAssuredApiAdapter adapter)) {
            return ExecutionResult.failure("SEND_REQUEST requires RestAssuredApiAdapter");
        }

        try {
            // Resolve template root from config
            String templatesRoot = resolveTemplatesRoot(context);
            Path absTemplatePath = Paths.get(templatesRoot).resolve(templatePath).normalize();

            // Build variable map: DataStore snapshot + test-data block (if provided)
            Map<String, Object> variables = buildVariableMap(step, context);

            // Merge template with variables → ApiRequest
            String apiBaseUrl = resolveApiBaseUrl(context);
            ApiRequest request = TemplateMerger.merge(absTemplatePath, variables, apiBaseUrl);

            // Execute
            ApiResponse response = adapter.send(request);

            // Store response for downstream actions
            context.getDataStore().put(DataScope.GLOBAL, LAST_RESPONSE_KEY, response);

            return ExecutionResult.success("API " + request.method() + " → " + response.statusCode());

        } catch (Exception ex) {
            return ExecutionResult.failure("SEND_REQUEST failed: " + ex.getMessage());
        }
    }

    private Map<String, Object> buildVariableMap(Step step, ExecutionContext context) {
        Map<String, Object> vars = new HashMap<>();

        // Seed with current DataStore global scope
        context.getDataStore().snapshot().forEach(
                (key, value) -> vars.put(key.getKey(), value)
        );

        // Merge test-data block if Source column points to a file
        ModifierSet mods = step.getModifiers();
        if (mods != null && mods.hasFilePath() && step.getKey() != null && !step.getKey().isBlank()) {
            try {
                String testDataRoot = resolveTestDataRoot(context);
                Path dataFilePath = Paths.get(testDataRoot).resolve(mods.getFilePath()).normalize();
                // Load the named block from the JSON file and merge
                Map<String, Object> block = loadDataBlock(dataFilePath, step.getKey());
                vars.putAll(block);
            } catch (Exception e) {
                // Non-fatal — continue with DataStore variables only
            }
        }

        return vars;
    }

    private Map<String, Object> loadDataBlock(Path filePath, String blockName) {
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            com.fasterxml.jackson.databind.JsonNode root = mapper.readTree(filePath.toFile());
            com.fasterxml.jackson.databind.JsonNode block = root.get(blockName);
            if (block == null) return java.util.Collections.emptyMap();
            return mapper.convertValue(block, new com.fasterxml.jackson.core.type.TypeReference<>() {});
        } catch (Exception e) {
            return java.util.Collections.emptyMap();
        }
    }

    private String resolveTemplatesRoot(ExecutionContext context) {
        String tp = context.getConfig() != null ? context.getConfig().getTemplatesPath() : null;
        return (tp != null && !tp.isBlank()) ? tp : "src/main/resources/templates";
    }

    private String resolveTestDataRoot(ExecutionContext context) {
        String td = context.getConfig() != null ? context.getConfig().getTestDataPath() : null;
        return (td != null && !td.isBlank()) ? td : "src/main/resources/testdata";
    }

    private String resolveApiBaseUrl(ExecutionContext context) {
        String base = context.getConfig() != null ? context.getConfig().getApiBaseUrl() : null;
        return (base != null) ? base : "";
    }
}
