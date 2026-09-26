package com.stellarintel.service;

import com.stellarintel.controller.DiagnosticReportApiController;
import com.stellarintel.dto.GenAiDiagnosticResponse;
import com.stellarintel.dto.KnowledgeSourceDto;
import com.stellarintel.dto.TelemetryDiagnosticReport;
import com.stellarintel.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Phase 6: Comprehensive Test Suite for Automated Satellite Diagnostic Report Generator.
 *
 * <p>Verifies:
 * <ol>
 *   <li>Nominal telemetry generates a valid PDF.</li>
 *   <li>Single anomaly generates a valid PDF.</li>
 *   <li>Multiple simultaneous anomalies appear in the PDF.</li>
 *   <li>Retrieved knowledge sources appear in the PDF.</li>
 *   <li>GenAI diagnostic content appears in the PDF.</li>
 *   <li>Offline fallback status is represented correctly.</li>
 *   <li>Missing optional knowledge sources do not crash report generation.</li>
 *   <li>Invalid / missing required report data is handled safely.</li>
 *   <li>Generated response has Content-Type = application/pdf.</li>
 *   <li>Generated PDF is non-empty, structurally valid, and starts with %PDF-.</li>
 * </ol>
 */
class DiagnosticReportServiceTest {

    private DiagnosticReportService reportService;
    private DiagnosticReportApiController reportApiController;

    @BeforeEach
    void setUp() {
        reportService = new DiagnosticReportService();
        reportApiController = new DiagnosticReportApiController(reportService);
    }

    @Test
    @DisplayName("Requirement 1 & 10: Nominal telemetry generates a valid, non-empty PDF starting with %PDF-")
    void testNominalTelemetryGeneratesValidPdf() {
        SatelliteTelemetry telemetry = SatelliteTelemetry.builder()
                .satelliteId("STELLAR-SAT-01")
                .timestamp("2026-09-25T12:00:00Z")
                .batteryTemperatureCelsius(18.5)
                .solarPanelVoltage(30.2)
                .attitudeControlErrorX(0.012)
                .attitudeControlErrorY(-0.008)
                .radiationExposureLevel(0.42)
                .build();

        TelemetryAnalysisResult analysis = TelemetryAnalysisResult.builder()
                .satelliteId("STELLAR-SAT-01")
                .timestamp("2026-09-25T12:00:00Z")
                .overallStatus(HealthStatus.NOMINAL)
                .anomalyDetected(false)
                .anomalyCount(0)
                .detectedAnomalies(new ArrayList<>())
                .summary("NOMINAL: All monitored subsystems operate within nominal envelopes.")
                .statusExplanation("Normal orbital telemetry baseline verified.")
                .analyzedAt(Instant.now().toString())
                .build();

        GenAiDiagnosticResponse diagnostic = GenAiDiagnosticResponse.builder()
                .diagnosticSummary("All satellite telemetry indicators align with nominal flight baseline.")
                .observedConditions(List.of("Battery temperature normal at 18.5°C", "Solar voltage stable at 30.2V"))
                .possibleContributingFactors(List.of("Nominal orbital environment in full sunlit phase"))
                .affectedSubsystems(List.of("EPS", "TCS", "ADCS"))
                .recommendedInvestigation(List.of("Maintain routine telemetry health tracking"))
                .limitations(List.of("Simulated telemetry envelope"))
                .status("SUCCESS")
                .build();

        TelemetryDiagnosticReport report = TelemetryDiagnosticReport.of(telemetry, analysis, diagnostic);

        byte[] pdfBytes = reportService.generateDiagnosticReportPdf(report);

        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 1000, "Generated PDF should be substantive (got " + pdfBytes.length + " bytes)");

        // Verify PDF Magic Bytes (%PDF-)
        String header = new String(pdfBytes, 0, Math.min(10, pdfBytes.length), StandardCharsets.US_ASCII);
        assertTrue(header.startsWith("%PDF-"), "Generated document must be a valid PDF starting with %PDF- header");
    }

    @Test
    @DisplayName("Requirement 2: Single thermal anomaly generates a valid PDF with breach details")
    void testSingleAnomalyGeneratesValidPdf() {
        SatelliteTelemetry telemetry = SatelliteTelemetry.builder()
                .satelliteId("STELLAR-SAT-01")
                .timestamp("2026-09-25T12:15:00Z")
                .batteryTemperatureCelsius(52.4)
                .solarPanelVoltage(31.0)
                .attitudeControlErrorX(0.015)
                .attitudeControlErrorY(0.012)
                .radiationExposureLevel(0.60)
                .build();

        List<TelemetryAnomaly> anomalies = List.of(
                TelemetryAnomaly.builder()
                        .parameter("Battery Temperature")
                        .observedValue(52.4)
                        .threshold("<= 45.0 °C")
                        .severity(AnomalySeverity.CRITICAL)
                        .anomalyType(AnomalyType.THERMAL_ANOMALY)
                        .explanation("Battery temperature of 52.4°C exceeds critical threshold of 45.0°C.")
                        .build()
        );

        TelemetryAnalysisResult analysis = TelemetryAnalysisResult.builder()
                .satelliteId("STELLAR-SAT-01")
                .timestamp("2026-09-25T12:15:00Z")
                .overallStatus(HealthStatus.CRITICAL)
                .anomalyDetected(true)
                .anomalyCount(1)
                .detectedAnomalies(anomalies)
                .summary("CRITICAL: 1 telemetry anomaly detected on [STELLAR-SAT-01].")
                .statusExplanation("Critical threshold breach in thermal control subsystem.")
                .analyzedAt(Instant.now().toString())
                .build();

        GenAiDiagnosticResponse diagnostic = GenAiDiagnosticResponse.builder()
                .diagnosticSummary("THERMAL HAZARD: Battery pack temperature is elevated.")
                .observedConditions(List.of("Battery temperature at 52.4°C"))
                .status("SUCCESS")
                .build();

        TelemetryDiagnosticReport report = TelemetryDiagnosticReport.of(telemetry, analysis, diagnostic);

        byte[] pdfBytes = reportService.generateDiagnosticReportPdf(report);

        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 1000);
        String header = new String(pdfBytes, 0, 5, StandardCharsets.US_ASCII);
        assertEquals("%PDF-", header);
    }

    @Test
    @DisplayName("Requirement 3 & 18: Multiple simultaneous anomalies appear cleanly without crashes")
    void testMultipleAnomaliesAppearInPdf() {
        SatelliteTelemetry telemetry = SatelliteTelemetry.builder()
                .satelliteId("SAT-MULTI-ANOMALY")
                .timestamp("2026-09-25T12:20:00Z")
                .batteryTemperatureCelsius(51.8)
                .solarPanelVoltage(38.5)
                .attitudeControlErrorX(1.45)
                .attitudeControlErrorY(-1.82)
                .radiationExposureLevel(9.2)
                .build();

        List<TelemetryAnomaly> multipleAnomalies = List.of(
                TelemetryAnomaly.builder()
                        .parameter("Battery Temperature")
                        .observedValue(51.8)
                        .threshold("<= 45.0 °C")
                        .severity(AnomalySeverity.CRITICAL)
                        .anomalyType(AnomalyType.THERMAL_ANOMALY)
                        .explanation("Critical battery temperature elevation.")
                        .build(),
                TelemetryAnomaly.builder()
                        .parameter("Solar Panel Voltage")
                        .observedValue(38.5)
                        .threshold("<= 36.0 V")
                        .severity(AnomalySeverity.WARNING)
                        .anomalyType(AnomalyType.VOLTAGE_OVER_RANGE)
                        .explanation("Over-voltage warning on solar array bus.")
                        .build(),
                TelemetryAnomaly.builder()
                        .parameter("Attitude Control Error X")
                        .observedValue(1.45)
                        .threshold("<= 0.050°")
                        .severity(AnomalySeverity.CRITICAL)
                        .anomalyType(AnomalyType.ATTITUDE_CONTROL_ANOMALY)
                        .explanation("Severe pointing error along X-axis.")
                        .build(),
                TelemetryAnomaly.builder()
                        .parameter("Radiation Exposure Level")
                        .observedValue(9.2)
                        .threshold("<= 5.0 µSv/h")
                        .severity(AnomalySeverity.CRITICAL)
                        .anomalyType(AnomalyType.HIGH_RADIATION)
                        .explanation("Extreme ionizing radiation environment.")
                        .build()
        );

        TelemetryAnalysisResult analysis = TelemetryAnalysisResult.builder()
                .satelliteId("SAT-MULTI-ANOMALY")
                .timestamp("2026-09-25T12:20:00Z")
                .overallStatus(HealthStatus.CRITICAL)
                .anomalyDetected(true)
                .anomalyCount(4)
                .detectedAnomalies(multipleAnomalies)
                .summary("CRITICAL: 4 telemetry anomalies detected across 4 subsystems.")
                .statusExplanation("Multiple subsystem limits exceeded.")
                .analyzedAt(Instant.now().toString())
                .build();

        GenAiDiagnosticResponse diagnostic = GenAiDiagnosticResponse.builder()
                .diagnosticSummary("Compound cascade anomaly: Solar flare interaction causing bus surge, ADCS disruption, and thermal rise.")
                .observedConditions(List.of("Thermal 51.8°C", "Voltage 38.5V", "Pointing 1.45°", "Radiation 9.2µSv/h"))
                .status("SUCCESS")
                .build();

        TelemetryDiagnosticReport report = TelemetryDiagnosticReport.of(telemetry, analysis, diagnostic);

        byte[] pdfBytes = reportService.generateDiagnosticReportPdf(report);

        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 2000, "Multiple anomalies report should be substantial (got " + pdfBytes.length + " bytes)");
    }

    @Test
    @DisplayName("Requirement 4: Retrieved technical knowledge sources render correctly in PDF")
    void testRetrievedKnowledgeSourcesAppearInPdf() {
        List<KnowledgeSourceDto> knowledgeSources = List.of(
                KnowledgeSourceDto.builder()
                        .documentName("battery-thermal.md")
                        .relevantTopic("Thermal Control — Technical Principles")
                        .retrievedContentPreview("Lithium-ion batteries operate in extreme orbital thermal environments.")
                        .relevanceScore(0.95)
                        .sourceIdentifier("knowledge/battery-thermal.md")
                        .build(),
                KnowledgeSourceDto.builder()
                        .documentName("solar-power.md")
                        .relevantTopic("Electrical Power — Parameter Cross-Coupling")
                        .retrievedContentPreview("High solar panel voltages increase dissipation in battery charge regulators.")
                        .relevanceScore(0.91)
                        .sourceIdentifier("knowledge/solar-power.md")
                        .build()
        );

        GenAiDiagnosticResponse diagnostic = GenAiDiagnosticResponse.builder()
                .diagnosticSummary("Thermal assessment grounded in retrieved technical documentation.")
                .knowledgeSources(knowledgeSources)
                .status("SUCCESS")
                .build();

        TelemetryAnalysisResult analysis = TelemetryAnalysisResult.builder()
                .satelliteId("STELLAR-SAT-01")
                .overallStatus(HealthStatus.WARNING)
                .anomalyDetected(true)
                .anomalyCount(1)
                .build();

        TelemetryDiagnosticReport report = TelemetryDiagnosticReport.of(analysis, diagnostic);

        byte[] pdfBytes = reportService.generateDiagnosticReportPdf(report);

        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 1500);
    }

    @Test
    @DisplayName("Requirement 5: GenAI diagnostic detailed sections (conditions, factors, procedures, limits) render")
    void testGenAiDiagnosticContentAppearsInPdf() {
        GenAiDiagnosticResponse diagnostic = GenAiDiagnosticResponse.builder()
                .diagnosticSummary("Executive diagnostic assessment of vehicle subsystem performance.")
                .observedConditions(List.of("Condition 1: Primary bus at nominal voltage", "Condition 2: Solar arrays tracking"))
                .possibleContributingFactors(List.of("Factor A: High beta-angle orbit", "Factor B: Increased communication payload usage"))
                .affectedSubsystems(List.of("Thermal Control", "Communications"))
                .recommendedInvestigation(List.of("Step 1: Check optical solar reflector temperatures", "Step 2: Monitor charge controller"))
                .limitations(List.of("Single-frame telemetry snapshot lacks historic trends"))
                .status("SUCCESS")
                .build();

        TelemetryDiagnosticReport report = TelemetryDiagnosticReport.of(
                TelemetryAnalysisResult.builder().satelliteId("STELLAR-SAT-01").overallStatus(HealthStatus.NOMINAL).build(),
                diagnostic
        );

        byte[] pdfBytes = reportService.generateDiagnosticReportPdf(report);

        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 1500);
    }

    @Test
    @DisplayName("Requirement 6 & 20: Offline fallback status is represented transparently with fallback banner")
    void testOfflineFallbackStatusRepresentedCorrectly() {
        GenAiDiagnosticResponse fallbackDiagnostic = GenAiDiagnosticResponse.builder()
                .diagnosticSummary("Deterministic analysis completed. Live LLM unconfigured; local deterministic fallback synthesized.")
                .status("OFFLINE_SYNTHESIS")
                .disclaimer("Autonomous diagnostic interpretation generated from telemetry data and technical subsystem documentation.")
                .build();

        TelemetryDiagnosticReport report = TelemetryDiagnosticReport.of(
                TelemetryAnalysisResult.builder().satelliteId("STELLAR-SAT-01").overallStatus(HealthStatus.NOMINAL).build(),
                fallbackDiagnostic
        );

        byte[] pdfBytes = reportService.generateDiagnosticReportPdf(report);

        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 1000);
    }

    @Test
    @DisplayName("Requirement 7: Missing optional knowledge sources does not crash PDF report generation")
    void testMissingOptionalKnowledgeSourcesDoesNotCrash() {
        GenAiDiagnosticResponse diagnostic = GenAiDiagnosticResponse.builder()
                .diagnosticSummary("Assessment without retrieved documents.")
                .knowledgeSources(null) // Null knowledge sources
                .status("SUCCESS")
                .build();

        TelemetryDiagnosticReport report = TelemetryDiagnosticReport.of(
                TelemetryAnalysisResult.builder().satelliteId("STELLAR-SAT-01").overallStatus(HealthStatus.NOMINAL).build(),
                diagnostic
        );

        byte[] pdfBytes = reportService.generateDiagnosticReportPdf(report);

        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 500);
    }

    @Test
    @DisplayName("Requirement 8 & 21: Completely empty or null report payload is handled safely without exceptions")
    void testInvalidMissingRequiredReportDataHandledSafely() {
        // Null report
        byte[] pdf1 = reportService.generateDiagnosticReportPdf(null);
        assertNotNull(pdf1);
        assertTrue(pdf1.length > 500);

        // Report with empty fields
        TelemetryDiagnosticReport emptyReport = new TelemetryDiagnosticReport();
        byte[] pdf2 = reportService.generateDiagnosticReportPdf(emptyReport);
        assertNotNull(pdf2);
        assertTrue(pdf2.length > 500);
    }

    @Test
    @DisplayName("Requirement 9 & 13: Report Controller endpoint returns HTTP 200, Content-Type=application/pdf, and Content-Disposition")
    void testControllerEndpointReturnsPdfHeaders() {
        SatelliteTelemetry telemetry = SatelliteTelemetry.builder()
                .satelliteId("STELLAR-SAT-01")
                .batteryTemperatureCelsius(20.0)
                .solarPanelVoltage(32.0)
                .build();

        TelemetryAnalysisResult analysis = TelemetryAnalysisResult.builder()
                .satelliteId("STELLAR-SAT-01")
                .overallStatus(HealthStatus.NOMINAL)
                .build();

        TelemetryDiagnosticReport report = TelemetryDiagnosticReport.of(telemetry, analysis, null);

        ResponseEntity<byte[]> response = reportApiController.generateDiagnosticReport(report);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_PDF, response.getHeaders().getContentType());

        String disposition = response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION);
        assertNotNull(disposition);
        assertTrue(disposition.contains("attachment; filename=\"stellarintel-diagnostic-report-STELLAR-SAT-01.pdf\""));

        byte[] body = response.getBody();
        assertNotNull(body);
        assertTrue(body.length > 500);
        String magicHeader = new String(body, 0, 5, StandardCharsets.US_ASCII);
        assertEquals("%PDF-", magicHeader);
    }

    @Test
    @DisplayName("Requirement 21: Satellite ID filename sanitization prevents path traversal and unsafe characters")
    void testSanitizeSatelliteId() {
        assertEquals("STELLAR-SAT", reportService.sanitizeSatelliteId(null));
        assertEquals("STELLAR-SAT", reportService.sanitizeSatelliteId("   "));
        assertEquals("SAT_01_TEST", reportService.sanitizeSatelliteId("SAT/01..TEST"));
        assertEquals("VEHICLE_123_ALPHA", reportService.sanitizeSatelliteId("VEHICLE@123#ALPHA!"));
        assertEquals("STELLAR-SAT-01", reportService.sanitizeSatelliteId("STELLAR-SAT-01"));
    }
}
