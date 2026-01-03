package com.hag.core.dev;

import com.hag.core.engine.DefaultExecutionEngine;
import com.hag.core.engine.ExecutionEngine;
import com.hag.core.reporting.engine.ConsoleReportEngine;
import com.hag.core.reporting.engine.DefaultEventPublisher;
import com.hag.core.reporting.engine.ReportEngine;

import java.util.List;

public class ExecutionEngineDevRunner {

    public static void main(String[] args) {

        ReportEngine reportEngine = new ConsoleReportEngine();
        DefaultEventPublisher publisher =
                new DefaultEventPublisher(List.of(reportEngine));

        ExecutionEngine engine =
                new DefaultExecutionEngine(publisher);

        reportEngine.startSuite();
        engine.execute("Sample_Test");
        reportEngine.endSuite();
    }
}
