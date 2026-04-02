package com.hag.dashboard.config;

import com.hag.dashboard.run.RunService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    private final RunService runService;

    public DashboardController(RunService runService) {
        this.runService = runService;
    }

    @GetMapping("/")
    public String dashboard(Model model) {
        var runs = runService.getAllRuns();
        long total  = runs.size();
        long passed = runs.stream().filter(r -> "PASSED".equals(r.getStatus())).count();
        long failed = runs.stream().filter(r -> "FAILED".equals(r.getStatus())).count();
        long errors = runs.stream().filter(r -> "ERROR".equals(r.getStatus())).count();

        model.addAttribute("totalRuns", total);
        model.addAttribute("passedRuns", passed);
        model.addAttribute("failedRuns", failed);
        model.addAttribute("errorRuns", errors);
        model.addAttribute("passRate", total > 0 ? Math.round(100.0 * passed / total) : 0);
        model.addAttribute("recentRuns", runs.stream().limit(10).toList());
        return "dashboard";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/settings")
    public String settings() {
        return "settings";
    }
}
