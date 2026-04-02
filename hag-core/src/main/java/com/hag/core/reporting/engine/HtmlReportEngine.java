package com.hag.core.reporting.engine;

import com.hag.core.reporting.events.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class HtmlReportEngine implements ReportEngine {

    private final String outputDirectory;
    private final String runName;
    private final LocalDateTime startTime;

    // testName -> Scenario node
    private final Map<String, ScenarioNode> scenarios = new ConcurrentHashMap<>();

    // Keep track of chronological order
    private final List<String> scenarioOrder = new ArrayList<>();

    private int totalTests = 0;
    private int passedTests = 0;
    private int failedTests = 0;

    public HtmlReportEngine(String runName) {
        this.runName = (runName != null && !runName.isBlank()) ? runName : "TestReport";
        this.outputDirectory = "TEST_RESULTS";
        this.startTime = LocalDateTime.now();
    }

    @Override
    public void startSuite() {
        try {
            Files.createDirectories(Paths.get(outputDirectory));
        } catch (IOException e) {
            System.err.println("Failed to create TEST_RESULTS directory: " + e.getMessage());
        }
    }

    @Override
    public void onEvent(Event event) {
        String testName = event.getTestName();
        // Ensure ScenarioNode exists
        ScenarioNode scenario = scenarios.computeIfAbsent(testName, k -> {
            scenarioOrder.add(k);
            return new ScenarioNode(k);
        });

        if (event instanceof TestStartedEvent e) {
            scenario.startTime = e.getTimestamp();
        } else if (event instanceof TestFinishedEvent e) {
            scenario.endTime = e.getEndTime();
            scenario.durationMs = e.getDurationMs();
            scenario.status = e.getStatus();

            totalTests++;
            if ("PASSED".equalsIgnoreCase(scenario.status)) {
                passedTests++;
            } else {
                failedTests++;
            }
        } else if (event instanceof StepStartedEvent e) {
            StepNode step = new StepNode();
            step.stepIndex = e.getStepIndex();
            
            String baseAction = e.getAction();
            if (baseAction != null && baseAction.contains("|")) {
                baseAction = baseAction.substring(0, baseAction.indexOf('|'));
            }
            if (baseAction != null && baseAction.contains(":")) {
                baseAction = baseAction.substring(0, baseAction.indexOf(':'));
            }

            if ("LOG".equalsIgnoreCase(baseAction)) {
                step.actionName = e.getKey() != null ? e.getKey() : "LOG";
            } else if ("SECTION".equalsIgnoreCase(baseAction)) {
                step.actionName = e.getKey() != null ? e.getKey() : "SECTION";
                step.isSection = true;
            } else {
                step.actionName = e.getAction();
            }

            step.startTime = e.getTimestamp();
            scenario.steps.put(e.getStepIndex(), step);
        } else if (event instanceof StepFinishedEvent e) {
            StepNode step = scenario.steps.get(e.getStepIndex());
            if (step != null) {
                step.status = e.getStatus();
                step.durationMs = e.getDurationMs();
                step.message = e.getMessage();
            }
        } else if (event instanceof StepFailedEvent e) {
            StepNode step = scenario.steps.get(e.getStepIndex());
            if (step != null) {
                step.status = "FAILED";
                step.errorMessage = e.getErrorMessage();
                step.failureType = e.getFailureType();
            }
        } else if (event instanceof ScreenshotCapturedEvent e) {
            StepNode step = scenario.steps.get(e.getStepIndex());
            if (step != null) {
                step.screenshotPath = e.getImagePath();
            }
        }
    }

    @Override
    public void endSuite() {
        generateHtmlReport();
    }

    private void generateHtmlReport() {
        String timestamp = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss").format(startTime);
        String fileName = runName.replaceAll("[^a-zA-Z0-9_-]", "_") + "_" + timestamp + ".html";
        Path filepath = Paths.get(outputDirectory, fileName);

        StringBuilder html = new StringBuilder();

        html.append("<!DOCTYPE html>\n");
        html.append("<html lang=\"en\">\n");
        html.append("<head>\n");
        html.append("    <meta charset=\"UTF-8\">\n");
        html.append("    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n");
        html.append("    <title>").append(runName).append(" - Test Report</title>\n");
        html.append("    <script src=\"https://cdn.jsdelivr.net/npm/chart.js\"></script>\n");
        html.append("    <style>\n");
        html.append("        body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #f4f7f6; color: #333; margin: 0; padding: 20px; }\n");
        html.append("        .container { max-width: 1200px; margin: auto; }\n");
        html.append("        .header { display: flex; justify-content: space-between; align-items: center; border-bottom: 2px solid #ddd; padding-bottom: 15px; margin-bottom: 20px; }\n");
        html.append("        .header h1 { margin: 0; color: #444; }\n");
        html.append("        .summary-dashboard { display: flex; gap: 20px; margin-bottom: 30px; }\n");
        html.append("        .chart-container { flex: 1; min-width: 250px; max-width: 300px; background: #fff; padding: 20px; border-radius: 8px; box-shadow: 0 4px 6px rgba(0,0,0,0.05); }\n");
        html.append("        .stats-container { flex: 2; display: flex; gap: 20px; justify-content: space-around; }\n");
        html.append("        .stat-card { flex: 1; background: #fff; padding: 20px; border-radius: 8px; box-shadow: 0 4px 6px rgba(0,0,0,0.05); text-align: center; display: flex; flex-direction: column; justify-content: center; }\n");
        html.append("        .stat-card h3 { margin: 0 0 10px 0; font-size: 1.1em; color: #666; }\n");
        html.append("        .stat-card .value { font-size: 2.5em; font-weight: bold; }\n");
        html.append("        .stat-card.passed .value { color: #28a745; }\n");
        html.append("        .stat-card.failed .value { color: #dc3545; }\n");
        html.append("        .stat-card.total .value { color: #007bff; }\n");
        html.append("        details { background: #fff; padding: 15px; border-radius: 8px; box-shadow: 0 4px 6px rgba(0,0,0,0.05); margin-bottom: 20px; }\n");
        html.append("        summary { font-size: 1.3em; font-weight: bold; cursor: pointer; outline: none; padding-bottom: 10px; border-bottom: 1px solid #eee; }\n");
        html.append("        .scenario-box { margin-top: 15px; border: 1px solid #ddd; border-radius: 6px; overflow: hidden; }\n");
        html.append("        .scenario-header { padding: 12px 15px; background: #f8f9fa; font-weight: bold; cursor: pointer; display: flex; justify-content: space-between; }\n");
        html.append("        .scenario-header:hover { background: #e2e6ea; }\n");
        html.append("        .scenario-content { display: none; padding: 15px; border-top: 1px solid #ddd; background: #fff; }\n");
        html.append("        .step-row { display: flex; padding: 10px; margin-bottom: 5px; border-radius: 4px; border-left: 5px solid transparent; align-items: flex-start; }\n");
        html.append("        .step-info { flex: 1; }\n");
        html.append("        .step-screenshot { margin-left: 15px; }\n");
        html.append("        .step-screenshot img { max-width: 200px; max-height: 150px; border: 1px solid #ccc; border-radius: 4px; cursor: pointer; transition: transform 0.2s; }\n");
        html.append("        .step-screenshot img:hover { transform: scale(1.05); }\n");
        html.append("        .status-PASSED { border-left-color: #28a745; background-color: #e9fbec; }\n");
        html.append("        .status-FAILED { border-left-color: #dc3545; background-color: #fce8e9; }\n");
        html.append("        .status-IGNORED { border-left-color: #ffc107; background-color: #fff9e6; }\n");
        html.append("        .status-badge { display: inline-block; padding: 3px 8px; border-radius: 12px; font-size: 0.8em; font-weight: bold; color: #fff; }\n");
        html.append("        .badge-PASSED { background-color: #28a745; }\n");
        html.append("        .badge-FAILED { background-color: #dc3545; }\n");
        html.append("        .badge-IGNORED { background-color: #ffc107; color: #333; }\n");
        html.append("        .errorMessage { color: #dc3545; font-family: monospace; white-space: pre-wrap; font-size: 0.9em; margin-top: 5px; }\n");
        html.append("    </style>\n");
        html.append("</head>\n");
        html.append("<body>\n");
        html.append("    <div class=\"container\">\n");
        html.append("        <div class=\"header\">\n");
        html.append("            <h1>").append(runName).append(" - Execution Report</h1>\n");
        html.append("            <div class=\"timestamp\">Executed on: ").append(startTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("</div>\n");
        html.append("        </div>\n");

        html.append("        <div class=\"summary-dashboard\">\n");
        html.append("            <div class=\"chart-container\">\n");
        html.append("                <canvas id=\"resultsChart\"></canvas>\n");
        html.append("            </div>\n");
        html.append("            <div class=\"stats-container\">\n");
        html.append("                <div class=\"stat-card passed\">\n");
        html.append("                    <h3>Passed Scenarios</h3>\n");
        html.append("                    <div class=\"value\">").append(passedTests).append("</div>\n");
        html.append("                </div>\n");
        html.append("                <div class=\"stat-card failed\">\n");
        html.append("                    <h3>Failed Scenarios</h3>\n");
        html.append("                    <div class=\"value\">").append(failedTests).append("</div>\n");
        html.append("                </div>\n");
        html.append("                <div class=\"stat-card total\">\n");
        html.append("                    <h3>Total Scenarios</h3>\n");
        html.append("                    <div class=\"value\">").append(totalTests).append("</div>\n");
        html.append("                </div>\n");
        html.append("            </div>\n");
        html.append("        </div>\n");

        html.append("        <details open>\n");
        html.append("            <summary>Failed Scenarios (").append(failedTests).append(")</summary>\n");
        for (String test : scenarioOrder) {
            ScenarioNode s = scenarios.get(test);
            if (!"PASSED".equalsIgnoreCase(s.status)) {
                renderScenario(html, s);
            }
        }
        if (failedTests == 0) html.append("<p style='padding:15px; color:#666;'>No failed scenarios. Great job!</p>\n");
        html.append("        </details>\n");

        html.append("        <details>\n");
        html.append("            <summary>Passed Scenarios (").append(passedTests).append(")</summary>\n");
        for (String test : scenarioOrder) {
            ScenarioNode s = scenarios.get(test);
            if ("PASSED".equalsIgnoreCase(s.status)) {
                renderScenario(html, s);
            }
        }
        if (passedTests == 0) html.append("<p style='padding:15px; color:#666;'>No passed scenarios.</p>\n");
        html.append("        </details>\n");

        html.append("    </div>\n");

        html.append("    <script>\n");
        html.append("        // Render Chart\n");
        html.append("        const ctx = document.getElementById('resultsChart').getContext('2d');\n");
        html.append("        new Chart(ctx, {\n");
        html.append("            type: 'doughnut',\n");
        html.append("            data: {\n");
        html.append("                labels: ['Passed', 'Failed'],\n");
        html.append("                datasets: [{\n");
        html.append("                    data: [").append(passedTests).append(", ").append(failedTests).append("],\n");
        html.append("                    backgroundColor: ['#28a745', '#dc3545'],\n");
        html.append("                    borderWidth: 0\n");
        html.append("                }]\n");
        html.append("            },\n");
        html.append("            options: {\n");
        html.append("                responsive: true,\n");
        html.append("                maintainAspectRatio: false,\n");
        html.append("                plugins: {\n");
        html.append("                    legend: { position: 'bottom' }\n");
        html.append("                }\n");
        html.append("            }\n");
        html.append("        });\n");
        html.append("\n");
        html.append("        // Accordion functionality for scenarios\n");
        html.append("        function toggleScenario(element) {\n");
        html.append("            const content = element.nextElementSibling;\n");
        html.append("            if (content.style.display === 'block') {\n");
        html.append("                content.style.display = 'none';\n");
        html.append("            } else {\n");
        html.append("                content.style.display = 'block';\n");
        html.append("            }\n");
        html.append("        }\n");
        html.append("    </script>\n");
        html.append("</body>\n");
        html.append("</html>\n");

        try {
            Files.writeString(filepath, html.toString());
            System.out.println("HAG → HTML Report generated: " + filepath.toAbsolutePath());
        } catch (IOException e) {
            System.err.println("Failed to write HTML report: " + e.getMessage());
        }
    }

    private void renderScenario(StringBuilder html, ScenarioNode s) {
        String badgeClass = "PASSED".equalsIgnoreCase(s.status) ? "badge-PASSED" : "badge-FAILED";
        if (s.status == null || s.status.isEmpty()) s.status = "IGNORED";
        if ("IGNORED".equalsIgnoreCase(s.status)) badgeClass = "badge-IGNORED";

        html.append("            <div class=\"scenario-box\">\n");
        html.append("                <div class=\"scenario-header\" onclick=\"toggleScenario(this)\">\n");
        html.append("                    <div class=\"scenario-name\">").append(s.testName).append("</div>\n");
        html.append("                    <div>\n");
        html.append("                        <span style=\"margin-right:15px; color:#666;\">").append(s.durationMs).append("ms</span>\n");
        html.append("                        <span class=\"status-badge ").append(badgeClass).append("\">").append(s.status).append("</span>\n");
        html.append("                    </div>\n");
        html.append("                </div>\n");
        html.append("                <div class=\"scenario-content\">\n");

        // Sort steps by stepIndex (they are populated async in older concurrent maps)
        s.steps.values().stream()
                .sorted((a, b) -> Integer.compare(a.stepIndex, b.stepIndex))
                .forEach(step -> renderStep(html, step));

        html.append("                </div>\n");
        html.append("            </div>\n");
    }

    private void renderStep(StringBuilder html, StepNode step) {
        if (step.isSection) {
            html.append("                    <div class=\"step-row\" style=\"border-bottom: 2px solid #ccc; background-color: #f8f9fa; border-left: 5px solid #6c757d; font-weight: bold; font-size: 1.1em;\">\n");
            html.append("                        <div class=\"step-info\">").append(step.actionName != null ? step.actionName : "SECTION").append("</div>\n");
            html.append("                    </div>\n");
            return;
        }

        String status = step.status != null ? step.status : "IGNORED";
        String rowClass = "status-" + status;

        html.append("                    <div class=\"step-row ").append(rowClass).append("\">\n");
        html.append("                        <div class=\"step-info\">\n");
        html.append("                            <strong>Step ").append(step.stepIndex).append(":</strong> ").append(step.actionName != null ? step.actionName : "Unknown Action").append("<br/>\n");
        if (step.message != null && !step.message.isBlank()) {
            html.append("                            <span style=\"color:#555;\">").append(step.message).append("</span><br/>\n");
        }
        if (step.errorMessage != null) {
            html.append("                            <div class=\"errorMessage\"><strong>").append(step.failureType).append(":</strong> ").append(step.errorMessage).append("</div>\n");
        }
        html.append("                            <small style=\"color:#999;\">Duration: ").append(step.durationMs).append("ms</small>\n");
        html.append("                        </div>\n");
        
        if (step.screenshotPath != null && !step.screenshotPath.isBlank()) {
            // Need relative path since the report is going into TEST_RESULTS and screenshot is probably in target/screenshots
            // We use simple relative path computation Assuming screenshot is an absolute path or relative to project root.
            // A simple trick is to convert everything to file URLs
            String fileUrl = Paths.get(step.screenshotPath).toAbsolutePath().toUri().toString();
            html.append("                        <div class=\"step-screenshot\">\n");
            html.append("                            <a href=\"").append(fileUrl).append("\" target=\"_blank\">\n");
            html.append("                               <img src=\"").append(fileUrl).append("\" alt=\"screenshot\"/>\n");
            html.append("                            </a>\n");
            html.append("                        </div>\n");
        }

        html.append("                    </div>\n");
    }

    // -- Internal Data Models --

    private static class ScenarioNode {
        String testName;
        long startTime;
        long endTime;
        String status = "PASSED"; // Default to passed unless a step fails
        long durationMs = 0;
        
        // Use map to ensure ordering by step index
        Map<Integer, StepNode> steps = new ConcurrentHashMap<>();

        ScenarioNode(String testName) {
            this.testName = testName;
        }
    }

    private static class StepNode {
        int stepIndex;
        String actionName;
        long startTime;
        String status = "STARTED"; // Default status
        long durationMs = 0;
        String message;
        String errorMessage;
        String failureType;
        String screenshotPath;
        boolean isSection = false;
    }
}
