package com.hag.dashboard.report;

import com.hag.dashboard.run.RunRecord;
import com.hag.dashboard.run.RunService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Controller
public class ReportController {

    private final RunService runService;

    @Value("${hag.results-dir:../TEST_RESULTS}")
    private String resultsDir;

    public ReportController(RunService runService) {
        this.runService = runService;
    }

    @GetMapping("/reports")
    public String reportsPage(
            @RequestParam(required = false) String status,
            Model model
    ) {
        if (status != null && !status.isBlank()) {
            model.addAttribute("runs", runService.getRunsByStatus(status));
            model.addAttribute("activeFilter", status);
        } else {
            model.addAttribute("runs", runService.getAllRuns());
            model.addAttribute("activeFilter", "ALL");
        }
        return "reports";
    }

    @GetMapping("/reports/{id}/view")
    public ResponseEntity<Resource> viewReport(@PathVariable String id) {
        return runService.getRun(id)
                .filter(r -> r.getReportPath() != null)
                .map(r -> {
                    Path path = Paths.get(r.getReportPath());
                    if (Files.exists(path)) {
                        return ResponseEntity.ok()
                                .contentType(MediaType.TEXT_HTML)
                                .body((Resource) new FileSystemResource(path));
                    }
                    return ResponseEntity.notFound().<Resource>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
