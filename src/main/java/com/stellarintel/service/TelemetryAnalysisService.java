package com.stellarintel.service;

import com.stellarintel.config.TelemetryThresholds;
import com.stellarintel.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Core Deterministic Telemetry Analysis Service for StellarIntel (Phase 2).
 *
 * <p>Architectural Purpose:
 * Evaluates raw numerical telemetry against centralized simulated engineering thresholds
 * using pure deterministic Java algorithms.
 *
 * <p>Phase Boundary Guarantee:
 * - NO Generative AI, LLMs, or prompt engineering are executed in this service.
 * - The structured {@link TelemetryAnalysisResult} produced here serves as the factual ground truth
 *   that will subsequently be passed to the GenAI diagnostic layer in later project phases.
 */
@Service
public class TelemetryAnalysisService {

    private static final Logger log = LoggerFactory.getLogger(TelemetryAnalysisService.class);
    private final TelemetryService telemetryService;

    public TelemetryAnalysisService(TelemetryService telemetryService) {
        this.telemetryService = telemetryService;
    }

    /**
     * Executes complete deterministic rule-based analysis on an ingested telemetry frame.
     *
     * @param telemetry valid satellite telemetry data
     * @return structured analysis result containing detected anomalies and derived health status
     */
    public TelemetryAnalysisResult analyzeTelemetry(SatelliteTelemetry telemetry) {
        // Enforce structural and sanity validation first
        telemetryService.validateTelemetry(telemetry);

        List<TelemetryAnomaly> anomalies = new ArrayList<>();

        // 1. Evaluate Thermal Subsystem (Battery Temperature)
        evaluateThermalSubsystem(telemetry, anomalies);

        // 2. Evaluate Radiation Dosimetry (Ionizing Radiation Flux)
        evaluateRadiationSubsystem(telemetry, anomalies);

        // 3. Evaluate ADCS (Attitude Determination & Control System Error Magnitude)
        evaluateAttitudeControlSubsystem(telemetry, anomalies);

        // 4. Evaluate EPS (Electrical Power System Solar Panel Voltage)
        evaluateElectricalPowerSubsystem(telemetry, anomalies);

        // 5. Derive System Health Status & Explainability
        HealthStatus overallStatus = deriveHealthStatus(anomalies);
        String statusExplanation = generateStatusExplanation(overallStatus, anomalies);
        String summary = generateDeterministicSummary(telemetry.getSatelliteId(), overallStatus, anomalies);

        TelemetryAnalysisResult result = TelemetryAnalysisResult.builder()
                .satelliteId(telemetry.getSatelliteId())
                .timestamp(telemetry.getTimestamp())
                .overallStatus(overallStatus)
                .anomalyDetected(!anomalies.isEmpty())
                .anomalyCount(anomalies.size())
                .detectedAnomalies(anomalies)
                .summary(summary)
                .statusExplanation(statusExplanation)
                .analyzedAt(Instant.now().toString())
                .build();

        log.info("Telemetry analysis completed for [{}]: status={}, anomalies={}",
                telemetry.getSatelliteId(), overallStatus, anomalies.size());

        return result;
    }

    /**
     * Rule A: Evaluates Battery Temperature against simulated thermal limits.
     * Nominal ceiling: <= 45.0°C.
     */
    private void evaluateThermalSubsystem(SatelliteTelemetry telemetry, List<TelemetryAnomaly> anomalies) {
        double temp = telemetry.getBatteryTemperatureCelsius();

        if (temp > TelemetryThresholds.BATTERY_TEMP_WARNING_CELSIUS) {
            AnomalySeverity severity = (temp > TelemetryThresholds.BATTERY_TEMP_CRITICAL_CELSIUS)
                    ? AnomalySeverity.CRITICAL
                    : AnomalySeverity.WARNING;

            String explanation = String.format(
                    "Battery temperature of %.1f°C exceeds nominal ceiling of %.1f°C%s",
                    temp,
                    TelemetryThresholds.BATTERY_TEMP_WARNING_CELSIUS,
                    severity == AnomalySeverity.CRITICAL
                            ? " (Severe thermal runaway hazard; exceeds critical threshold of 50.0°C)."
                            : " (Elevated thermal dissipation; operating within caution band)."
            );

            anomalies.add(TelemetryAnomaly.builder()
                    .parameter("Battery Temperature")
                    .observedValue(temp)
                    .threshold(String.format("<= %.1f °C", TelemetryThresholds.BATTERY_TEMP_WARNING_CELSIUS))
                    .severity(severity)
                    .anomalyType(AnomalyType.THERMAL_ANOMALY)
                    .explanation(explanation)
                    .build());
        }
    }

    /**
     * Rule B: Evaluates Radiation Sensor reading against simulated space radiation flux limits.
     * Nominal threshold: <= 5.0 µSv/h.
     */
    private void evaluateRadiationSubsystem(SatelliteTelemetry telemetry, List<TelemetryAnomaly> anomalies) {
        double rad = telemetry.getRadiationExposureLevel();

        if (rad > TelemetryThresholds.RADIATION_WARNING_LEVEL) {
            AnomalySeverity severity = (rad >= TelemetryThresholds.RADIATION_CRITICAL_LEVEL)
                    ? AnomalySeverity.CRITICAL
                    : AnomalySeverity.WARNING;

            String explanation = String.format(
                    "Radiation exposure of %.2f µSv/h exceeds safe baseline of %.1f µSv/h%s",
                    rad,
                    TelemetryThresholds.RADIATION_WARNING_LEVEL,
                    severity == AnomalySeverity.CRITICAL
                            ? " (Extreme ionizing flux detected; possible solar flare or Van Allen belt crossing)."
                            : " (Elevated particle flux detected; dosimeter indicates warning level)."
            );

            anomalies.add(TelemetryAnomaly.builder()
                    .parameter("Radiation Exposure")
                    .observedValue(rad)
                    .threshold(String.format("<= %.1f µSv/h", TelemetryThresholds.RADIATION_WARNING_LEVEL))
                    .severity(severity)
                    .anomalyType(AnomalyType.HIGH_RADIATION)
                    .explanation(explanation)
                    .build());
        }
    }

    /**
     * Rule C: Evaluates ADCS angular pointing error magnitude.
     * Error magnitude = sqrt(attitudeControlErrorX² + attitudeControlErrorY²).
     * Nominal tolerance: <= 0.05°.
     */
    private void evaluateAttitudeControlSubsystem(SatelliteTelemetry telemetry, List<TelemetryAnomaly> anomalies) {
        double errX = telemetry.getAttitudeControlErrorX();
        double errY = telemetry.getAttitudeControlErrorY();

        double magnitude = Math.sqrt((errX * errX) + (errY * errY));

        if (magnitude > TelemetryThresholds.ATTITUDE_ERROR_MAGNITUDE_WARNING_DEG) {
            AnomalySeverity severity = (magnitude > TelemetryThresholds.ATTITUDE_ERROR_MAGNITUDE_CRITICAL_DEG)
                    ? AnomalySeverity.CRITICAL
                    : AnomalySeverity.WARNING;

            String explanation = String.format(
                    "ADCS pointing error magnitude of %.4f° (X: %.4f°, Y: %.4f°) exceeds fine pointing limit of %.3f°%s",
                    magnitude,
                    errX,
                    errY,
                    TelemetryThresholds.ATTITUDE_ERROR_MAGNITUDE_WARNING_DEG,
                    severity == AnomalySeverity.CRITICAL
                            ? " (Critical attitude perturbation; potential reaction wheel desaturation or tumble risk)."
                            : " (Subsystem pointing error in deadband recovery range)."
            );

            anomalies.add(TelemetryAnomaly.builder()
                    .parameter("Attitude Control Error (ADCS)")
                    .observedValue(Math.round(magnitude * 10000.0) / 10000.0)
                    .threshold(String.format("<= %.3f °", TelemetryThresholds.ATTITUDE_ERROR_MAGNITUDE_WARNING_DEG))
                    .severity(severity)
                    .anomalyType(AnomalyType.ATTITUDE_CONTROL_ANOMALY)
                    .explanation(explanation)
                    .build());
        }
    }

    /**
     * Rule D: Evaluates Electrical Power System Solar Array Bus Voltage.
     * Nominal simulated operating range: 26.0 V to 35.0 V.
     */
    private void evaluateElectricalPowerSubsystem(SatelliteTelemetry telemetry, List<TelemetryAnomaly> anomalies) {
        double voltage = telemetry.getSolarPanelVoltage();

        if (voltage < TelemetryThresholds.SOLAR_VOLTAGE_MIN_NOMINAL_VOLTS) {
            AnomalySeverity severity = (voltage <= TelemetryThresholds.SOLAR_VOLTAGE_CRITICAL_LOW_VOLTS)
                    ? AnomalySeverity.CRITICAL
                    : AnomalySeverity.WARNING;

            String explanation = String.format(
                    "Solar array voltage of %.1f V is below nominal minimum operating threshold of %.1f V%s",
                    voltage,
                    TelemetryThresholds.SOLAR_VOLTAGE_MIN_NOMINAL_VOLTS,
                    severity == AnomalySeverity.CRITICAL
                            ? " (Critical power depletion; risks battery deep-discharge lockout)."
                            : " (Under-voltage condition; array experiencing partial occultation or load peak)."
            );

            anomalies.add(TelemetryAnomaly.builder()
                    .parameter("Solar Panel Voltage")
                    .observedValue(voltage)
                    .threshold(String.format(">= %.1f V", TelemetryThresholds.SOLAR_VOLTAGE_MIN_NOMINAL_VOLTS))
                    .severity(severity)
                    .anomalyType(AnomalyType.VOLTAGE_UNDER_RANGE)
                    .explanation(explanation)
                    .build());

        } else if (voltage > TelemetryThresholds.SOLAR_VOLTAGE_MAX_NOMINAL_VOLTS) {
            AnomalySeverity severity = (voltage >= TelemetryThresholds.SOLAR_VOLTAGE_CRITICAL_HIGH_VOLTS)
                    ? AnomalySeverity.CRITICAL
                    : AnomalySeverity.WARNING;

            String explanation = String.format(
                    "Solar array voltage of %.1f V exceeds nominal maximum operating threshold of %.1f V%s",
                    voltage,
                    TelemetryThresholds.SOLAR_VOLTAGE_MAX_NOMINAL_VOLTS,
                    severity == AnomalySeverity.CRITICAL
                            ? " (Critical over-voltage; risk of shunt regulator breakdown)."
                            : " (Over-voltage condition; array cold-soak peak or regulator overshoot)."
            );

            anomalies.add(TelemetryAnomaly.builder()
                    .parameter("Solar Panel Voltage")
                    .observedValue(voltage)
                    .threshold(String.format("<= %.1f V", TelemetryThresholds.SOLAR_VOLTAGE_MAX_NOMINAL_VOLTS))
                    .severity(severity)
                    .anomalyType(AnomalyType.VOLTAGE_OVER_RANGE)
                    .explanation(explanation)
                    .build());
        }
    }

    /**
     * Derives overall health status using clear severity rules:
     * - No anomalies -> NOMINAL
     * - Any CRITICAL anomaly OR 2+ concurrent anomalies -> CRITICAL
     * - Exactly 1 WARNING anomaly -> WARNING
     */
    private HealthStatus deriveHealthStatus(List<TelemetryAnomaly> anomalies) {
        if (anomalies.isEmpty()) {
            return HealthStatus.NOMINAL;
        }

        boolean hasCriticalAnomaly = anomalies.stream()
                .anyMatch(a -> a.getSeverity() == AnomalySeverity.CRITICAL);

        if (hasCriticalAnomaly || anomalies.size() >= 2) {
            return HealthStatus.CRITICAL;
        }

        return HealthStatus.WARNING;
    }

    /**
     * Generates a clear, explainable justification for the assigned overall status.
     */
    private String generateStatusExplanation(HealthStatus status, List<TelemetryAnomaly> anomalies) {
        return switch (status) {
            case NOMINAL -> "All monitored telemetry parameters are operating within standard simulated operating boundaries.";
            case WARNING -> String.format(
                    "Elevated risk: 1 warning-level anomaly detected (%s). Spacecraft functional but requires monitoring.",
                    anomalies.get(0).getParameter()
            );
            case CRITICAL -> {
                boolean hasCritical = anomalies.stream().anyMatch(a -> a.getSeverity() == AnomalySeverity.CRITICAL);
                if (hasCritical && anomalies.size() >= 2) {
                    yield String.format(
                            "Severe multi-system risk: %d concurrent anomalies detected including critical subsystem limits.",
                            anomalies.size()
                    );
                } else if (hasCritical) {
                    yield "Critical threshold breach: One or more parameters have exceeded major engineering safety envelopes.";
                } else {
                    yield String.format(
                            "Compound anomaly risk: %d separate warning-level anomalies detected simultaneously across subsystems.",
                            anomalies.size()
                    );
                }
            }
        };
    }

    /**
     * Generates a concise human-readable deterministic summary.
     */
    private String generateDeterministicSummary(String satelliteId, HealthStatus status, List<TelemetryAnomaly> anomalies) {
        if (anomalies.isEmpty()) {
            return String.format(
                    "NOMINAL: Satellite [%s] telemetry parameters are all within nominal operating envelopes. Subsystems operating normally.",
                    satelliteId
            );
        }

        String impactedSubsystems = anomalies.stream()
                .map(TelemetryAnomaly::getParameter)
                .collect(Collectors.joining(", "));

        return String.format(
                "%s: %d telemetry %s detected on [%s]. Impacted subsystem(s): %s. Deterministic rules require %s.",
                status,
                anomalies.size(),
                anomalies.size() == 1 ? "anomaly" : "anomalies",
                satelliteId,
                impactedSubsystems,
                status == HealthStatus.CRITICAL ? "immediate engineering intervention" : "elevated telemetry tracking"
        );
    }
}
