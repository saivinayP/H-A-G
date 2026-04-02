package com.hag.dashboard.run;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;

@Controller
public class RunController {

    private final RunService runService;
    private final TestDiscoveryService discovery;

    public RunController(RunService runService, TestDiscoveryService discovery) {
        this.runService = runService;
        this.discovery  = discovery;
    }

    // ── Run Tests page ──────────────────────────────────────

    @GetMapping("/run")
    public String runPage(Model model) {
        model.addAttribute("testTree", discovery.getTestTree());
        model.addAttribute("environments", discovery.getEnvironments());
        model.addAttribute("defaults", discovery.getRunnerDefaults());
        return "run";
    }

    // ── Trigger run ─────────────────────────────────────────

    @PostMapping("/api/run")
    public String triggerRun(
            @RequestParam String suite,
            @RequestParam(defaultValue = "chrome") String browser,
            @RequestParam(defaultValue = "dev") String environment,
            @RequestParam(defaultValue = "local") String mode,
            @RequestParam(defaultValue = "") String gridUrl,
            @RequestParam(defaultValue = "1") int threadCount,
            @RequestParam(defaultValue = "AT_FAILED_STEP") String screenshotLevel,
            @RequestParam(defaultValue = "false") boolean headless,
            Authentication auth
    ) {
        String user = auth != null ? auth.getName() : "anonymous";
        RunRecord record = runService.trigger(
                suite, browser, environment, mode, gridUrl,
                threadCount, screenshotLevel, headless, user
        );
        return "redirect:/run/" + record.getRunId();
    }

    // ── Live run page ───────────────────────────────────────

    @GetMapping("/run/{id}")
    public String livePage(@PathVariable String id, Model model) {
        model.addAttribute("runId", id);
        model.addAttribute("run", runService.getRun(id).orElse(null));
        return "live";
    }

    // ── SSE endpoint ────────────────────────────────────────

    @GetMapping(value = "/api/run/{id}/stream", produces = "text/event-stream")
    @ResponseBody
    public SseEmitter stream(@PathVariable String id) {
        return runService.subscribe(id);
    }
}
