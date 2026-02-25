package com.hag.core.engine;

import com.hag.core.model.Step;

import java.util.ArrayList;
import java.util.List;

public final class StepFlowSplitter {

    private static final String FINALLY_DIRECTIVE = "FINALLY";

    private StepFlowSplitter() {}

    public static Flow split(List<Step> steps) {

        List<Step> main = new ArrayList<>();
        List<Step> fin = new ArrayList<>();

        boolean finallyMode = false;

        for (Step step : steps) {

            if (isFinallyDirective(step)) {
                finallyMode = true;
                continue;
            }

            if (finallyMode) {
                fin.add(step);
            } else {
                main.add(step);
            }
        }

        return new Flow(main, fin);
    }

    private static boolean isFinallyDirective(Step step) {
        return FINALLY_DIRECTIVE.equalsIgnoreCase(step.getAction());
    }

    public record Flow(List<Step> main, List<Step> fin) {}
}