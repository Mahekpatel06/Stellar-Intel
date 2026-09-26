package com.stellarintel;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class StellarIntelApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Context loads successfully")
    void contextLoads() {
    }

    @Test
    @DisplayName("Root URL loads the Mission Control dashboard")
    void shouldLoadMissionControlDashboard() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("STELLARINTEL")))
                .andExpect(content().string(containsString("SIMULATION MODE")));
    }

    @Test
    @DisplayName("GET /api/telemetry/profiles returns all 3 simulated profiles")
    void shouldReturnSimulatedProfiles() throws Exception {
        mockMvc.perform(get("/api/telemetry/profiles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasKey("nominal-orbit")))
                .andExpect(jsonPath("$", hasKey("solar-flare")))
                .andExpect(jsonPath("$", hasKey("reaction-wheel-anomaly")));
    }

    @Test
    @DisplayName("POST /api/telemetry/validate succeeds for valid payload")
    void shouldValidateValidTelemetryPayload() throws Exception {
        String validJson = """
                {
                    "satelliteId": "STELLAR-SAT-01",
                    "timestamp": "2026-09-25T12:00:00Z",
                    "batteryTemperatureCelsius": 20.5,
                    "solarPanelVoltage": 31.0,
                    "attitudeControlErrorX": 0.01,
                    "attitudeControlErrorY": -0.02,
                    "radiationExposureLevel": 0.45
                }
                """;

        mockMvc.perform(post("/api/telemetry/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.telemetry.satelliteId").value("STELLAR-SAT-01"));
    }

    @Test
    @DisplayName("POST /api/telemetry/validate returns 400 for missing satelliteId")
    void shouldRejectInvalidTelemetryPayload() throws Exception {
        String invalidJson = """
                {
                    "satelliteId": "",
                    "timestamp": "2026-09-25T12:00:00Z",
                    "batteryTemperatureCelsius": 20.5,
                    "solarPanelVoltage": 31.0,
                    "attitudeControlErrorX": 0.01,
                    "attitudeControlErrorY": -0.02,
                    "radiationExposureLevel": 0.45
                }
                """;

        mockMvc.perform(post("/api/telemetry/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("satelliteId")));
    }

    @Test
    @DisplayName("POST /api/telemetry/validate returns 400 for malformed JSON")
    void shouldHandleMalformedJsonGracefully() throws Exception {
        String malformedJson = "{ satelliteId: 'unquoted', brokenJson... }";

        mockMvc.perform(post("/api/telemetry/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(malformedJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("Malformed JSON")));
    }

    @Test
    @DisplayName("POST /api/telemetry/analyze executes deterministic analysis on nominal telemetry")
    void shouldAnalyzeNominalTelemetrySuccessfully() throws Exception {
        String nominalJson = """
                {
                    "satelliteId": "STELLAR-SAT-01",
                    "timestamp": "2026-09-25T12:00:00Z",
                    "batteryTemperatureCelsius": 18.5,
                    "solarPanelVoltage": 30.2,
                    "attitudeControlErrorX": 0.012,
                    "attitudeControlErrorY": -0.008,
                    "radiationExposureLevel": 0.42
                }
                """;

        mockMvc.perform(post("/api/telemetry/analyze")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(nominalJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.satelliteId").value("STELLAR-SAT-01"))
                .andExpect(jsonPath("$.overallStatus").value("NOMINAL"))
                .andExpect(jsonPath("$.anomalyDetected").value(false))
                .andExpect(jsonPath("$.anomalyCount").value(0))
                .andExpect(jsonPath("$.detectedAnomalies", hasSize(0)))
                .andExpect(jsonPath("$.summary", containsString("NOMINAL")));
    }

    @Test
    @DisplayName("POST /api/telemetry/analyze flags radiation anomaly and assigns CRITICAL overall status")
    void shouldAnalyzeSolarFlareAndDetectCriticalAnomaly() throws Exception {
        String solarFlareJson = """
                {
                    "satelliteId": "STELLAR-SAT-01",
                    "timestamp": "2026-09-25T12:05:00Z",
                    "batteryTemperatureCelsius": 38.2,
                    "solarPanelVoltage": 34.8,
                    "attitudeControlErrorX": 0.035,
                    "attitudeControlErrorY": 0.041,
                    "radiationExposureLevel": 8.75
                }
                """;

        mockMvc.perform(post("/api/telemetry/analyze")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(solarFlareJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.satelliteId").value("STELLAR-SAT-01"))
                .andExpect(jsonPath("$.overallStatus").value("CRITICAL"))
                .andExpect(jsonPath("$.anomalyDetected").value(true))
                .andExpect(jsonPath("$.detectedAnomalies[*].anomalyType", hasItem("HIGH_RADIATION")))
                .andExpect(jsonPath("$.summary", containsString("CRITICAL")));
    }

    @Test
    @DisplayName("POST /api/telemetry/analyze rejects invalid payload with 400")
    void shouldRejectAnalyzeRequestForInvalidTelemetry() throws Exception {
        String invalidJson = """
                {
                    "satelliteId": "",
                    "timestamp": "2026-09-25T12:00:00Z",
                    "batteryTemperatureCelsius": 18.5,
                    "solarPanelVoltage": 30.2,
                    "attitudeControlErrorX": 0.012,
                    "attitudeControlErrorY": -0.008,
                    "radiationExposureLevel": 0.42
                }
                """;

        mockMvc.perform(post("/api/telemetry/analyze")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("satelliteId")));
    }

    @Test
    @DisplayName("POST /api/telemetry/diagnose executes unified deterministic + GenAI diagnostic pipeline")
    void shouldDiagnoseNominalTelemetrySuccessfully() throws Exception {
        String nominalJson = """
                {
                    "satelliteId": "STELLAR-SAT-01",
                    "timestamp": "2026-09-25T12:00:00Z",
                    "batteryTemperatureCelsius": 18.5,
                    "solarPanelVoltage": 30.2,
                    "attitudeControlErrorX": 0.012,
                    "attitudeControlErrorY": -0.008,
                    "radiationExposureLevel": 0.42
                }
                """;

        mockMvc.perform(post("/api/telemetry/diagnose")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(nominalJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.analysis.satelliteId").value("STELLAR-SAT-01"))
                .andExpect(jsonPath("$.analysis.overallStatus").value("NOMINAL"))
                .andExpect(jsonPath("$.diagnostic.diagnosticSummary").isNotEmpty())
                .andExpect(jsonPath("$.diagnostic.observedConditions").isArray())
                .andExpect(jsonPath("$.diagnostic.affectedSubsystems").isArray())
                .andExpect(jsonPath("$.diagnostic.recommendedInvestigation").isArray());
    }

    @Test
    @DisplayName("POST /api/telemetry/diagnose handles Solar Flare anomaly with complete diagnostic explanation")
    void shouldDiagnoseSolarFlareSuccessfully() throws Exception {
        String solarFlareJson = """
                {
                    "satelliteId": "STELLAR-SAT-01",
                    "timestamp": "2026-09-25T12:05:00Z",
                    "batteryTemperatureCelsius": 38.2,
                    "solarPanelVoltage": 34.8,
                    "attitudeControlErrorX": 0.035,
                    "attitudeControlErrorY": 0.041,
                    "radiationExposureLevel": 8.75
                }
                """;

        mockMvc.perform(post("/api/telemetry/diagnose")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(solarFlareJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.analysis.overallStatus").value("CRITICAL"))
                .andExpect(jsonPath("$.diagnostic.diagnosticSummary", containsString("RADIATION")))
                .andExpect(jsonPath("$.diagnostic.observedConditions", hasItem(containsString("8.75"))));
    }

    @Test
    @DisplayName("POST /api/telemetry/diagnose rejects invalid telemetry with 400 Bad Request")
    void shouldRejectDiagnoseWithMissingSatelliteId() throws Exception {
        String invalidJson = """
                {
                    "satelliteId": "",
                    "timestamp": "2026-09-25T12:00:00Z",
                    "batteryTemperatureCelsius": 18.5,
                    "solarPanelVoltage": 30.2,
                    "attitudeControlErrorX": 0.012,
                    "attitudeControlErrorY": -0.008,
                    "radiationExposureLevel": 0.42
                }
                """;

        mockMvc.perform(post("/api/telemetry/diagnose")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("satelliteId")));
    }

    @Test
    @DisplayName("POST /api/reports/diagnostic returns downloadable PDF with correct headers and magic bytes")
    void shouldGenerateDiagnosticPdfReport() throws Exception {
        String reportJson = """
                {
                    "telemetry": {
                        "satelliteId": "STELLAR-SAT-01",
                        "timestamp": "2026-09-25T12:00:00Z",
                        "batteryTemperatureCelsius": 18.5,
                        "solarPanelVoltage": 30.2,
                        "attitudeControlErrorX": 0.012,
                        "attitudeControlErrorY": -0.008,
                        "radiationExposureLevel": 0.42
                    },
                    "analysis": {
                        "satelliteId": "STELLAR-SAT-01",
                        "timestamp": "2026-09-25T12:00:00Z",
                        "overallStatus": "NOMINAL",
                        "anomalyDetected": false,
                        "anomalyCount": 0,
                        "detectedAnomalies": [],
                        "summary": "All systems nominal.",
                        "statusExplanation": "Nominal flight envelopes."
                    },
                    "diagnostic": {
                        "diagnosticSummary": "Autonomous diagnosis nominal.",
                        "status": "SUCCESS"
                    }
                }
                """;

        mockMvc.perform(post("/api/reports/diagnostic")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reportJson))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/pdf"))
                .andExpect(header().string("Content-Disposition", containsString("stellarintel-diagnostic-report-STELLAR-SAT-01.pdf")))
                .andExpect(result -> {
                    byte[] content = result.getResponse().getContentAsByteArray();
                    org.junit.jupiter.api.Assertions.assertNotNull(content);
                    org.junit.jupiter.api.Assertions.assertTrue(content.length > 500);
                    org.junit.jupiter.api.Assertions.assertEquals("%PDF-", new String(content, 0, 5, java.nio.charset.StandardCharsets.US_ASCII));
                });
    }
}
