package com.hag.core.executor;

import com.hag.core.context.ExecutionContext;
import com.hag.core.dispatcher.descriptor.ActionDescriptor;
import com.hag.core.model.Step;
import com.hag.core.result.ExecutionResult;

import java.math.BigDecimal;
import java.util.regex.Pattern;

/**
 * COMPARE action — compares two values using a specified operator.
 *
 * <h3>CSV Syntax</h3>
 * <pre>
 *   COMPARE:EQUALS,actual,,expected
 *   COMPARE:NOT_EQUALS,actual,,expected
 *   COMPARE:CONTAINS,actual,,expected
 *   COMPARE:NOT_CONTAINS,actual,,expected
 *   COMPARE:STARTS_WITH,actual,,expected
 *   COMPARE:ENDS_WITH,actual,,expected
 *   COMPARE:GT,${UI:price},,100
 *   COMPARE:LT,${UI:count},,50
 *   COMPARE:GTE,${UI:stock},,1
 *   COMPARE:LTE,${UI:qty},,10
 *   COMPARE:REGEX,${UI:orderId},,ORD-\d{6}
 * </pre>
 *
 * <ul>
 *   <li><b>Recipient</b> — actual value or {@code ${VAR}} expression</li>
 *   <li><b>Key</b>       — expected value or pattern</li>
 *   <li><b>Sub-case</b>  — comparison operator (see list above)</li>
 * </ul>
 */
public final class CompareAction implements Action {

    @Override
    public String name() {
        return "COMPARE";
    }

    @Override
    public ActionCategory category() {
        return ActionCategory.CONTEXT;
    }

    @Override
    public ExecutionResult execute(Step step, ActionDescriptor descriptor, ExecutionContext context) {

        // Resolve actual value from Recipient column
        Object actualObj = context.resolveValue(step.getRecipient());
        String actual = actualObj == null ? "" : actualObj.toString().trim();

        // Resolve expected value from Key column
        Object expectedObj = context.resolveValue(step.getKey());
        String expected = expectedObj == null ? "" : expectedObj.toString().trim();

        String op = descriptor.hasSubCase() ? descriptor.subCase() : "EQUALS";

        try {
            boolean passed = switch (op.toUpperCase()) {
                case "EQUALS"       -> actual.equals(expected);
                case "NOT_EQUALS"   -> !actual.equals(expected);
                case "CONTAINS"     -> actual.contains(expected);
                case "NOT_CONTAINS" -> !actual.contains(expected);
                case "STARTS_WITH"  -> actual.startsWith(expected);
                case "ENDS_WITH"    -> actual.endsWith(expected);
                case "GT"           -> numericCompare(actual, expected) > 0;
                case "LT"           -> numericCompare(actual, expected) < 0;
                case "GTE"          -> numericCompare(actual, expected) >= 0;
                case "LTE"          -> numericCompare(actual, expected) <= 0;
                case "REGEX"        -> Pattern.compile(expected).matcher(actual).find();
                default             -> throw new IllegalArgumentException(
                                               "Unknown COMPARE operator: [" + op + "]"
                                       );
            };

            if (passed) {
                return ExecutionResult.success();
            }

            return ExecutionResult.failure(
                    "COMPARE:" + op + " failed — actual: [" + actual + "] expected: [" + expected + "]"
            );

        } catch (IllegalArgumentException ex) {
            return ExecutionResult.failure(ex.getMessage());
        } catch (Exception ex) {
            return ExecutionResult.failure("COMPARE error: " + ex.getMessage());
        }
    }

    private int numericCompare(String actual, String expected) {
        try {
            BigDecimal a = new BigDecimal(actual);
            BigDecimal b = new BigDecimal(expected);
            return a.compareTo(b);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(
                    "COMPARE numeric operator requires numeric values — got: ["
                            + actual + "] and [" + expected + "]"
            );
        }
    }
}
