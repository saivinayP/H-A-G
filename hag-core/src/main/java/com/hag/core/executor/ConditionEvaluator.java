package com.hag.core.executor;

import com.hag.core.context.ExecutionContext;
import com.hag.core.resolver.StepValueResolver;

import java.util.regex.Pattern;

/**
 * Evaluates conditions for control flow actions like EXECUTE:IF, SKIP:IF.
 * <p>
 * Supported operators:
 * =, !=, >, <, >=, <=, contains, not_contains, starts_with, ends_with, matches, is_blank, is_not_blank
 */
public final class ConditionEvaluator {

    private ConditionEvaluator() {}

    /**
     * Evaluates a raw expression string against the current execution context.
     * Note: The expression is evaluated as "Left [operator] Right", where Left usually contains variables
     * and Right is a literal or another variable.
     */
    public static boolean evaluate(String expression, ExecutionContext context) {
        if (expression == null || expression.isBlank()) {
            return false;
        }

        Object res = context.resolveValue(expression);
        String resolved = res == null ? "" : res.toString();

        // Unary operators first
        if (resolved.endsWith(" is_blank")) {
            return resolved.substring(0, resolved.length() - 9).trim().isBlank();
        }
        if (resolved.endsWith(" is_not_blank")) {
            return !resolved.substring(0, resolved.length() - 13).trim().isBlank();
        }

        // Binary operators
        String[] ops = {"!=", ">=", "<=", "=", ">", "<", " contains ", " not_contains ", " starts_with ", " ends_with ", " matches "};
        
        for (String op : ops) {
            int idx = resolved.indexOf(op);
            if (idx != -1) {
                String left = resolved.substring(0, idx).trim();
                String right = resolved.substring(idx + op.length()).trim();
                return compare(left, op.trim(), right);
            }
        }

        // Default: if it's just a boolean representation
        return Boolean.parseBoolean(resolved.trim());
    }

    private static boolean compare(String left, String op, String right) {
        switch (op) {
            case "=": return left.equalsIgnoreCase(right);
            case "!=": return !left.equalsIgnoreCase(right);
            case "contains": return left.contains(right);
            case "not_contains": return !left.contains(right);
            case "starts_with": return left.startsWith(right);
            case "ends_with": return left.endsWith(right);
            case "matches": return Pattern.compile(right).matcher(left).find();
        }

        // Numeric comparisons
        if (op.equals(">") || op.equals("<") || op.equals(">=") || op.equals("<=")) {
            try {
                double l = Double.parseDouble(left);
                double r = Double.parseDouble(right);
                switch (op) {
                    case ">": return l > r;
                    case "<": return l < r;
                    case ">=": return l >= r;
                    case "<=": return l <= r;
                }
            } catch (NumberFormatException e) {
                return false;
            }
        }

        return false;
    }
}
