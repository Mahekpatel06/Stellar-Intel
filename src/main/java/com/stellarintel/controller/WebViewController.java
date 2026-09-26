package com.stellarintel.controller;

import com.stellarintel.service.TelemetryService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller serving the Thymeleaf space-themed Mission Control dashboard.
 */
@Controller
public class WebViewController {

    private final TelemetryService telemetryService;

    public WebViewController(TelemetryService telemetryService) {
        this.telemetryService = telemetryService;
    }

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("appName", "STELLARINTEL");
        model.addAttribute("appSubtitle", "Satellite Telemetry Analysis AI");
        model.addAttribute("phase", "Phase 1 - Base Foundation & Telemetry Ingestion");
        model.addAttribute("profiles", telemetryService.getSimulatedProfiles());
        return "index";
    }
}
