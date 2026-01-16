package com.hag.core.engine.resolver;

import com.hag.core.engine.context.ExecutionContext;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class TemplateResolver {

    private static final Pattern VAR_PATTERN = Pattern.compile("\\$(\\w+)\\$");

    private TemplateResolver() {
    }

    public static String resolve(
            String template,
            Map<String, Object> testData,
            ExecutionContext context) {

        Matcher matcher = VAR_PATTERN.matcher(template);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String var = matcher.group(1);

            Object value = testData.containsKey(var)
                    ? testData.get(var)
                    : context.resolveValue("${" + var + "}");

            if (value == null) {
                throw new IllegalStateException(
                        "No value found for template variable: " + var
                );
            }

            matcher.appendReplacement(result, Matcher.quoteReplacement(value.toString()));
        }

        matcher.appendTail(result);
        return result.toString();
    }
}
