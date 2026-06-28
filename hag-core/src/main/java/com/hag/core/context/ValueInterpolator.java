package com.hag.core.context;

import com.hag.core.data.DataGenerator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ValueInterpolator {

    private static final Pattern PATTERN =
            Pattern.compile("\\$\\{([^}]+)}");

    private ValueInterpolator() {}

    public static String interpolate(
            String input,
            ExecutionContext context
    ) {
        if (input == null || !input.contains("${")) {
            return input;
        }

        Matcher matcher = PATTERN.matcher(input);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String token = matcher.group(1);
            String resolved = resolveToken(token, context);
            matcher.appendReplacement(result, resolved);
        }

        matcher.appendTail(result);
        return result.toString();
    }

    private static String resolveToken(
            String token,
            ExecutionContext context
    ) {
        // Check if token is a data generation function (e.g. RANDOM_EMAIL)
        if (DataGenerator.isGenerationFunction(token)) {
            return DataGenerator.generate(token);
        }

        String key = token;

        // Legacy compat: strip scope prefix if present (e.g. UI:myVar -> myVar)
        if (token.contains(":")) {
            String[] parts = token.split(":", 2);
            key = parts[1];
        }

        return context.getDataStore()
                .get(key)
                .map(Object::toString)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "No value found for placeholder: ${"
                                        + token + "}"
                        )
                );
    }
}
