package hag.core.engine;

import hag.core.engine.model.Step;
import hag.core.engine.dispatcher.ControlActions;

import java.util.ArrayList;
import java.util.List;

public final class StepFlowSplitter {

    private StepFlowSplitter() {}

    public static Flow split(List<Step> steps) {
        List<Step> main = new ArrayList<>();
        List<Step> fin = new ArrayList<>();

        boolean finallyMode = false;

        for (Step step : steps) {
            if (ControlActions.FINALLY.equalsIgnoreCase(step.getAction())) {
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

    public record Flow(List<Step> main, List<Step> fin) {}
}
