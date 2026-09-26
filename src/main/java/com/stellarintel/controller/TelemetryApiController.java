package com.stellarintel.controller;

import com.stellarintel.dto.GenAiDiagnosticResponse;
import com.stellarintel.dto.TelemetryDiagnosticReport;
import com.stellarintel.dto.TelemetryIngestResponse;
import com.stellarintel.model.SatelliteTelemetry;
import com.stellarintel.model.TelemetryAnalysisResult;
import com.stellarintel.service.GenAiDiagnosticService;
import com.stellarintel.service.TelemetryAnalysisService;
import com.stellarintel.service.TelemetryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST API Controller for telemetry validation, parsing, simulated profiles retrieval,
 * deterministic anomaly analysis, and Spring AI diagnostic reasoning.
 */
@RestController
@RequestMapping("/api/telemetry")
public class TelemetryApiController {

    private final TelemetryService telemetryService;
    private final TelemetryAnalysisService telemetryAnalysisService;
    private final GenAiDiagnosticService genAiDiagnosticService;

    public TelemetryApiController(TelemetryService telemetryService,
                                  TelemetryAnalysisService telemetryAnalysisService,
                                  GenAiDiagnosticService genAiDiagnosticService) {
        this.telemetryService = telemetryService;
        this.telemetryAnalysisService = telemetryAnalysisService;
        this.genAiDiagnosticService = genAiDiagnosticService;
    }

    /**
     * Retrieves the three predefined simulated telemetry profiles.
     */
    @GetMapping("/profiles")
    public ResponseEntity<Map<String, SatelliteTelemetry>> getProfiles() {
        return ResponseEntity.ok(telemetryService.getSimulatedProfiles());
    }

    /**
     * Retrieves a single simulated telemetry profile by key.
     */
    @GetMapping("/profiles/{key}")
    public ResponseEntity<SatelliteTelemetry> getProfile(@PathVariable String key) {
        return ResponseEntity.ok(telemetryService.getProfileByKey(key));
    }

    /**
     * Validates and ingests a structured telemetry object.
     */
    @PostMapping("/validate")
    public ResponseEntity<TelemetryIngestResponse> validateTelemetry(@RequestBody SatelliteTelemetry telemetry) {
        telemetryService.validateTelemetry(telemetry);
        return ResponseEntity.ok(
                TelemetryIngestResponse.ok("Telemetry payload successfully validated and ingested.", telemetry)
        );
    }

    /**
     * Parses and validates raw JSON text directly.
     */
    @PostMapping("/parse-raw")
    public ResponseEntity<TelemetryIngestResponse> parseRawTelemetry(@RequestBody String rawJson) {
        SatelliteTelemetry telemetry = telemetryService.parseAndValidateRawJson(rawJson);
        return ResponseEntity.ok(
                TelemetryIngestResponse.ok("Raw telemetry JSON successfully parsed and verified.", telemetry)
        );
    }

    /**
     * Executes deterministic rule-based telemetry analysis (Phase 2).
     */
    @PostMapping("/analyze")
    public ResponseEntity<TelemetryAnalysisResult> analyzeTelemetry(@RequestBody SatelliteTelemetry telemetry) {
        TelemetryAnalysisResult result = telemetryAnalysisService.analyzeTelemetry(telemetry);
        return ResponseEntity.ok(result);
    }

    /**
     * Executes the Phase 3 GenAI Diagnostic Pipeline:
     * 1. Validates numerical telemetry.
     * 2. Executes deterministic analysis (Ground Truth).
     * 3. Invokes Spring AI ChatModel via GenAiDiagnosticService to generate structured explanation.
     * 4. Returns unified TelemetryDiagnosticReport containing both deterministic facts and GenAI interpretation.
     */
    @PostMapping("/diagnose")
    public ResponseEntity<TelemetryDiagnosticReport> diagnoseTelemetry(@RequestBody SatelliteTelemetry telemetry) {
        // Step 1 & 2: Authoritative deterministic analysis
        TelemetryAnalysisResult analysis = telemetryAnalysisService.analyzeTelemetry(telemetry);

        // Step 3: GenAI Technical Interpretation
        GenAiDiagnosticResponse diagnostic = genAiDiagnosticService.diagnoseTelemetry(telemetry, analysis);

        // Step 4: Return combined report containing telemetry, analysis, and diagnostic
        return ResponseEntity.ok(TelemetryDiagnosticReport.of(telemetry, analysis, diagnostic));
    }
}
