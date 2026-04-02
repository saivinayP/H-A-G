package com.hag.dashboard.run;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "run_records")
public class RunRecord {

    @Id
    @Column(length = 64)
    private String runId;

    @Column(nullable = false)
    private String suite;

    private String browser;
    private String environment;
    private String mode;
    private String gridUrl;
    private int threadCount;
    private String screenshotLevel;
    private boolean headless;

    @Column(nullable = false)
    private String status = "RUNNING";  // RUNNING | PASSED | FAILED | ERROR

    private String triggeredBy;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;
    private String reportPath;

    private int totalTests;
    private int passedTests;
    private int failedTests;

    public RunRecord() {}

    public RunRecord(String runId, String suite, String browser, String environment,
                     String mode, String gridUrl, int threadCount, String screenshotLevel,
                     boolean headless, String triggeredBy) {
        this.runId           = runId;
        this.suite           = suite;
        this.browser         = browser;
        this.environment     = environment;
        this.mode            = mode;
        this.gridUrl         = gridUrl;
        this.threadCount     = threadCount;
        this.screenshotLevel = screenshotLevel;
        this.headless        = headless;
        this.triggeredBy     = triggeredBy;
        this.startedAt       = LocalDateTime.now();
        this.status          = "RUNNING";
    }

    // ── Getters / Setters ───────────────────────────────────

    public String getRunId()               { return runId; }
    public void setRunId(String runId)     { this.runId = runId; }

    public String getSuite()               { return suite; }
    public void setSuite(String suite)     { this.suite = suite; }

    public String getBrowser()             { return browser; }
    public void setBrowser(String b)       { this.browser = b; }

    public String getEnvironment()             { return environment; }
    public void setEnvironment(String env)     { this.environment = env; }

    public String getMode()                { return mode; }
    public void setMode(String mode)       { this.mode = mode; }

    public String getGridUrl()             { return gridUrl; }
    public void setGridUrl(String u)       { this.gridUrl = u; }

    public int getThreadCount()            { return threadCount; }
    public void setThreadCount(int t)      { this.threadCount = t; }

    public String getScreenshotLevel()                  { return screenshotLevel; }
    public void setScreenshotLevel(String s)            { this.screenshotLevel = s; }

    public boolean isHeadless()            { return headless; }
    public void setHeadless(boolean h)     { this.headless = h; }

    public String getStatus()              { return status; }
    public void setStatus(String s)        { this.status = s; }

    public String getTriggeredBy()                 { return triggeredBy; }
    public void setTriggeredBy(String t)           { this.triggeredBy = t; }

    public LocalDateTime getStartedAt()            { return startedAt; }
    public void setStartedAt(LocalDateTime t)      { this.startedAt = t; }

    public LocalDateTime getFinishedAt()           { return finishedAt; }
    public void setFinishedAt(LocalDateTime t)     { this.finishedAt = t; }

    public String getReportPath()                  { return reportPath; }
    public void setReportPath(String p)            { this.reportPath = p; }

    public int getTotalTests()             { return totalTests; }
    public void setTotalTests(int t)       { this.totalTests = t; }

    public int getPassedTests()            { return passedTests; }
    public void setPassedTests(int p)      { this.passedTests = p; }

    public int getFailedTests()            { return failedTests; }
    public void setFailedTests(int f)      { this.failedTests = f; }
}
