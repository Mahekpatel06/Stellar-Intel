package com.stellarintel.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stellarintel.exception.InvalidTelemetryException;
import com.stellarintel.model.AnomalySeverity;
import com.stellarintel.model.AnomalyType;
import com.stellarintel.model.HealthStatus;
import com.stellarintel.model.SatelliteTelemetry;
import com.stellarintel.model.TelemetryAnalysisResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TelemetryAnalysisServiceTest {

    private TelemetryService telemetryService;
    private TelemetryAnalysisService analysisService;

    @BeforeEach
    void setUp() {
        telemetryService = new TelemetryService(new ObjectMapper());
        analysisService = new TelemetryAnalysisService(telemetryService);
    }

    @Test
    @DisplayName("TEST 1 - Nominal Orbit: Evaluates with zero anomalies and NOMINAL status")
    void testNominalOrbitProfile_ShouldBeNominalWithNoAnomalies() {
        SatelliteTelemetry nominal = telemetryService.getProfileByKey("nominal-orbit");

        TelemetryAnalysisResult result = analysisService.analyzeTelemetry(nominal);

        assertNotNull(result);
        assertEquals("STELLAR-SAT-01", result.getSatelliteId());
        assertEquals(HealthStatus.NOMINAL, result.getOverallStatus());
        assertFalse(result.isAnomalyDetected());
        assertEquals(0, result.getAnomalyCount());
        assertTrue(result.getDetectedAnomalies().isEmpty());
        assertTrue(result.getSummary().contains("NOMINAL"));
    }

    @Test
    @DisplayName("TEST 2 - Solar Flare Event: Detects radiation anomaly and classifies severity")
    void testSolarFlareProfile_ShouldDetectHighRadiation() {
        SatelliteTelemetry solarFlare = telemetryService.getProfileByKey("solar-flare");

        TelemetryAnalysisResult result = analysisService.analyzeTelemetry(solarFlare);

        assertNotNull(result);
        assertTrue(result.isAnomalyDetected());
        assertTrue(result.getAnomalyCount() >= 1);

        boolean hasRadiationAnomaly = result.getDetectedAnomalies().stream()
                .anyMatch(a -> a.getAnomalyType() == AnomalyType.HIGH_RADIATION
                        && a.getSeverity() == AnomalySeverity.CRITICAL
                        && a.getObservedValue() == 8.75);

        assertTrue(hasRadiationAnomaly, "Should detect high radiation anomaly with CRITICAL severity (8.75 >= 8.0)");
        assertEquals(HealthStatus.CRITICAL, result.getOverallStatus());
        assertTrue(result.getSummary().contains("CRITICAL"));
    }

    @Test
    @DisplayName("TEST 3 - Reaction Wheel Anomaly: Computes error magnitude and flags CRITICAL ADCS anomaly")
    void testReactionWheelProfile_ShouldDetectAttitudeError() {
        SatelliteTelemetry reactionWheel = telemetryService.getProfileByKey("reaction-wheel-anomaly");

        TelemetryAnalysisResult result = analysisService.analyzeTelemetry(reactionWheel);

        assertNotNull(result);
        assertTrue(result.isAnomalyDetected());
        assertEquals(1, result.getAnomalyCount());

        boolean hasAttitudeAnomaly = result.getDetectedAnomalies().stream()
                .anyMatch(a -> a.getAnomalyType() == AnomalyType.ATTITUDE_CONTROL_ANOMALY
                        && a.getSeverity() == AnomalySeverity.CRITICAL);

        assertTrue(hasAttitudeAnomaly, "Attitude magnitude should exceed 0.50° critical threshold");
        assertEquals(HealthStatus.CRITICAL, result.getOverallStatus());
        assertTrue(result.getSummary().contains("CRITICAL"));
    }

    @Test
    @DisplayName("Thermal Anomaly: Detects warning and critical overheating conditions")
    void testThermalAnomalyThresholds() {
        // Warning level: 46.5°C (> 45.0, <= 50.0)
        SatelliteTelemetry warningTelemetry = SatelliteTelemetry.builder()
                .satelliteId("TEST-SAT")
                .timestamp("2026-09-25T12:00:00Z")
                .batteryTemperatureCelsius(46.5)
                .solarPanelVoltage(30.0)
                .attitudeControlErrorX(0.01)
                .attitudeControlErrorY(0.01)
                .radiationExposureLevel(1.0)
                .build();

        TelemetryAnalysisResult warningResult = analysisService.analyzeTelemetry(warningTelemetry);
        assertEquals(1, warningResult.getAnomalyCount());
        assertEquals(AnomalySeverity.WARNING, warningResult.getDetectedAnomalies().get(0).getSeverity());
        assertEquals(HealthStatus.WARNING, warningResult.getOverallStatus());

        // Critical level: 52.0°C (> 50.0)
        SatelliteTelemetry criticalTelemetry = SatelliteTelemetry.builder()
                .satelliteId("TEST-SAT")
                .timestamp("2026-09-25T12:00:00Z")
                .batteryTemperatureCelsius(52.0)
                .solarPanelVoltage(30.0)
                .attitudeControlErrorX(0.01)
                .attitudeControlErrorY(0.01)
                .radiationExposureLevel(1.0)
                .build();

        TelemetryAnalysisResult criticalResult = analysisService.analyzeTelemetry(criticalTelemetry);
        assertEquals(1, criticalResult.getAnomalyCount());
        assertEquals(AnomalySeverity.CRITICAL, criticalResult.getDetectedAnomalies().get(0).getSeverity());
        assertEquals(HealthStatus.CRITICAL, criticalResult.getOverallStatus());
    }

    @Test
    @DisplayName("EPS Voltage Anomaly: Detects under-voltage and over-voltage conditions")
    void testSolarVoltageThresholds() {
        // Under-voltage warning: 24.5V (< 26.0, > 22.0)
        SatelliteTelemetry underVoltage = SatelliteTelemetry.builder()
                .satelliteId("TEST-SAT")
                .timestamp("2026-09-25T12:00:00Z")
                .batteryTemperatureCelsius(20.0)
                .solarPanelVoltage(24.5)
                .attitudeControlErrorX(0.01)
                .attitudeControlErrorY(0.01)
                .radiationExposureLevel(1.0)
                .build();

        TelemetryAnalysisResult underResult = analysisService.analyzeTelemetry(underVoltage);
        assertEquals(1, underResult.getAnomalyCount());
        assertEquals(AnomalyType.VOLTAGE_UNDER_RANGE, underResult.getDetectedAnomalies().get(0).getAnomalyType());
        assertEquals(AnomalySeverity.WARNING, underResult.getDetectedAnomalies().get(0).getSeverity());

        // Over-voltage critical: 39.0V (>= 38.0)
        SatelliteTelemetry overVoltage = SatelliteTelemetry.builder()
                .satelliteId("TEST-SAT")
                .timestamp("2026-09-25T12:00:00Z")
                .batteryTemperatureCelsius(20.0)
                .solarPanelVoltage(39.0)
                .attitudeControlErrorX(0.01)
                .attitudeControlErrorY(0.01)
                .radiationExposureLevel(1.0)
                .build();

        TelemetryAnalysisResult overResult = analysisService.analyzeTelemetry(overVoltage);
        assertEquals(1, overResult.getAnomalyCount());
        assertEquals(AnomalyType.VOLTAGE_OVER_RANGE, overResult.getDetectedAnomalies().get(0).getAnomalyType());
        assertEquals(AnomalySeverity.CRITICAL, overResult.getDetectedAnomalies().get(0).getSeverity());
        assertEquals(HealthStatus.CRITICAL, overResult.getOverallStatus());
    }

    @Test
    @DisplayName("Multiple Anomalies: Concurrent warning anomalies escalate overall status to CRITICAL")
    void testMultipleWarningAnomaliesEscalateToCritical() {
        // 2 separate warning anomalies: radiation 6.0 (> 5.0) and under-voltage 25.0 (< 26.0)
        SatelliteTelemetry multiAnomaly = SatelliteTelemetry.builder()
                .satelliteId("TEST-SAT")
                .timestamp("2026-09-25T12:00:00Z")
                .batteryTemperatureCelsius(25.0)
                .solarPanelVoltage(25.0)
                .attitudeControlErrorX(0.01)
                .attitudeControlErrorY(0.01)
                .radiationExposureLevel(6.0)
                .build();

        TelemetryAnalysisResult result = analysisService.analyzeTelemetry(multiAnomaly);
        assertEquals(2, result.getAnomalyCount());
        assertEquals(HealthStatus.CRITICAL, result.getOverallStatus());
        assertTrue(result.getStatusExplanation().contains("Compound anomaly risk"));
    }

    @Test
    @DisplayName("Boundary Value Testing: Exact thresholds maintain expected classifications")
    void testBoundaryValues() {
        // Exactly on nominal limits: 45.0°C temp, 5.0 rad, 30.0V, magnitude 0.050°
        // sqrt(0.03^2 + 0.04^2) = sqrt(0.0009 + 0.0016) = sqrt(0.0025) = 0.050 exactly!
        SatelliteTelemetry boundaryNominal = SatelliteTelemetry.builder()
                .satelliteId("BOUNDARY-SAT")
                .timestamp("2026-09-25T12:00:00Z")
                .batteryTemperatureCelsius(45.0)
                .solarPanelVoltage(30.0)
                .attitudeControlErrorX(0.03)
                .attitudeControlErrorY(0.04)
                .radiationExposureLevel(5.0)
                .build();

        TelemetryAnalysisResult result = analysisService.analyzeTelemetry(boundaryNominal);
        assertEquals(HealthStatus.NOMINAL, result.getOverallStatus(), "Boundary values on threshold ceilings must remain NOMINAL");
        assertEquals(0, result.getAnomalyCount());
    }

    @Test
    @DisplayName("Validation Rejection: Incomplete or null telemetry throws InvalidTelemetryException")
    void testInvalidTelemetryRejection() {
        assertThrows(InvalidTelemetryException.class, () -> analysisService.analyzeTelemetry(null));

        SatelliteTelemetry blankId = SatelliteTelemetry.builder()
                .satelliteId("  ")
                .timestamp("2026-09-25T12:00:00Z")
                .batteryTemperatureCelsius(20.0)
                .solarPanelVoltage(30.0)
                .attitudeControlErrorX(0.01)
                .attitudeControlErrorY(0.01)
                .radiationExposureLevel(1.0)
                .build();

        assertThrows(InvalidTelemetryException.class, () -> analysisService.analyzeTelemetry(blankId));
    }
}
