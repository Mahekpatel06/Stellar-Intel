package com.stellarintel.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stellarintel.exception.InvalidTelemetryException;
import com.stellarintel.model.SatelliteTelemetry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Service responsible for validating, parsing, and providing simulated telemetry profiles.
 *
 * NOTE FOR PHASE 1:
 * - Deterministic anomaly detection rules and GenAI analysis pipelines will be implemented in Phases 2-5.
 * - This service provides basic structural validation and mock profile retrieval.
 */
@Service
public class TelemetryService {

    private static final Logger log = LoggerFactory.getLogger(TelemetryService.class);
    private final ObjectMapper objectMapper;
    private final Map<String, SatelliteTelemetry> simulatedProfiles;

    public TelemetryService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.simulatedProfiles = initializeSimulatedProfiles();
    }

    /**
     * Initializes the three predefined simulated telemetry profiles.
     * All values are synthetic and designed for educational/research simulation.
     */
    private Map<String, SatelliteTelemetry> initializeSimulatedProfiles() {
        Map<String, SatelliteTelemetry> profiles = new LinkedHashMap<>();

        profiles.put("nominal-orbit", SatelliteTelemetry.builder()
                .satelliteId("STELLAR-SAT-01")
                .timestamp("2026-09-25T12:00:00Z")
                .batteryTemperatureCelsius(18.5)
                .solarPanelVoltage(30.2)
                .attitudeControlErrorX(0.012)
                .attitudeControlErrorY(-0.008)
                .radiationExposureLevel(0.42)
                .build());

        profiles.put("solar-flare", SatelliteTelemetry.builder()
                .satelliteId("STELLAR-SAT-01")
                .timestamp("2026-09-25T12:05:00Z")
                .batteryTemperatureCelsius(38.2)
                .solarPanelVoltage(34.8)
                .attitudeControlErrorX(0.035)
                .attitudeControlErrorY(0.041)
                .radiationExposureLevel(8.75)
                .build());

        profiles.put("reaction-wheel-anomaly", SatelliteTelemetry.builder()
                .satelliteId("STELLAR-SAT-01")
                .timestamp("2026-09-25T12:10:00Z")
                .batteryTemperatureCelsius(22.1)
                .solarPanelVoltage(28.7)
                .attitudeControlErrorX(1.845)
                .attitudeControlErrorY(-2.312)
                .radiationExposureLevel(0.55)
                .build());

        profiles.put("thermal-anomaly", SatelliteTelemetry.builder()
                .satelliteId("STELLAR-SAT-01")
                .timestamp("2026-09-25T12:15:00Z")
                .batteryTemperatureCelsius(52.4)
                .solarPanelVoltage(31.0)
                .attitudeControlErrorX(0.015)
                .attitudeControlErrorY(0.012)
                .radiationExposureLevel(0.60)
                .build());

        profiles.put("multiple-anomalies", SatelliteTelemetry.builder()
                .satelliteId("STELLAR-SAT-01")
                .timestamp("2026-09-25T12:20:00Z")
                .batteryTemperatureCelsius(51.8)
                .solarPanelVoltage(38.5)
                .attitudeControlErrorX(1.450)
                .attitudeControlErrorY(-1.820)
                .radiationExposureLevel(9.20)
                .build());

        return Collections.unmodifiableMap(profiles);
    }

    /**
     * Retrieves all available simulated telemetry profiles.
     */
    public Map<String, SatelliteTelemetry> getSimulatedProfiles() {
        return simulatedProfiles;
    }

    /**
     * Retrieves a specific simulated profile by key.
     */
    public SatelliteTelemetry getProfileByKey(String profileKey) {
        SatelliteTelemetry profile = simulatedProfiles.get(profileKey);
        if (profile == null) {
            throw new InvalidTelemetryException("Unknown simulated profile key: " + profileKey);
        }
        return profile;
    }

    /**
     * Parses a raw JSON string into a validated SatelliteTelemetry object.
     */
    public SatelliteTelemetry parseAndValidateRawJson(String rawJson) {
        if (rawJson == null || rawJson.trim().isEmpty()) {
            throw new InvalidTelemetryException("Telemetry payload cannot be empty.");
        }

        try {
            SatelliteTelemetry telemetry = objectMapper.readValue(rawJson, SatelliteTelemetry.class);
            validateTelemetry(telemetry);
            return telemetry;
        } catch (JsonProcessingException e) {
            throw new InvalidTelemetryException("Failed to parse telemetry JSON: " + e.getOriginalMessage(), e);
        }
    }

    /**
     * Validates structural requirements and numerical sanity of telemetry data.
     * Enforces strict physical bounds, ISO-8601 format, and finite numeric values.
     */
    public void validateTelemetry(SatelliteTelemetry telemetry) {
        if (telemetry == null) {
            throw new InvalidTelemetryException("Telemetry object cannot be null.");
        }

        if (telemetry.getSatelliteId() == null || telemetry.getSatelliteId().trim().isEmpty()) {
            throw new InvalidTelemetryException("Field 'satelliteId' is required and cannot be blank.");
        }

        if (telemetry.getSatelliteId().length() > 64) {
            throw new InvalidTelemetryException("Field 'satelliteId' exceeds maximum allowed length of 64 characters.");
        }

        if (telemetry.getTimestamp() == null || telemetry.getTimestamp().trim().isEmpty()) {
            throw new InvalidTelemetryException("Field 'timestamp' is required and cannot be blank.");
        }

        try {
            java.time.format.DateTimeFormatter.ISO_DATE_TIME.parse(telemetry.getTimestamp().trim());
        } catch (Exception dtEx) {
            throw new InvalidTelemetryException("Field 'timestamp' must be a valid ISO-8601 formatted date-time string (e.g., '2026-09-25T12:00:00Z').");
        }

        // 1. Battery Pack Temperature Validation (-273.15°C to +300.0°C)
        double temp = telemetry.getBatteryTemperatureCelsius();
        if (Double.isNaN(temp) || Double.isInfinite(temp)) {
            throw new InvalidTelemetryException("Field 'batteryTemperatureCelsius' must be a valid finite number.");
        }
        if (temp < -273.15 || temp > 300.0) {
            throw new InvalidTelemetryException(String.format(
                    "Field 'batteryTemperatureCelsius' contains an unrealistic value (%.2f °C). Acceptable physical range: -273.15°C to 300.0°C.", temp));
        }

        // 2. Solar Array Bus Voltage Validation (0.0V to 500.0V)
        double voltage = telemetry.getSolarPanelVoltage();
        if (Double.isNaN(voltage) || Double.isInfinite(voltage)) {
            throw new InvalidTelemetryException("Field 'solarPanelVoltage' must be a valid finite number.");
        }
        if (voltage < 0.0 || voltage > 500.0) {
            throw new InvalidTelemetryException(String.format(
                    "Field 'solarPanelVoltage' cannot be negative or exceed physical array limits (%.2f V). Acceptable physical range: 0.0V to 500.0V.", voltage));
        }

        // 3. ADCS Attitude Error X and Y Validation (-360.0° to +360.0°)
        double attX = telemetry.getAttitudeControlErrorX();
        if (Double.isNaN(attX) || Double.isInfinite(attX)) {
            throw new InvalidTelemetryException("Field 'attitudeControlErrorX' must be a valid finite number.");
        }
        if (attX < -360.0 || attX > 360.0) {
            throw new InvalidTelemetryException(String.format(
                    "Field 'attitudeControlErrorX' exceeds physical angular limits (%.2f°). Acceptable range: -360.0° to 360.0°.", attX));
        }

        double attY = telemetry.getAttitudeControlErrorY();
        if (Double.isNaN(attY) || Double.isInfinite(attY)) {
            throw new InvalidTelemetryException("Field 'attitudeControlErrorY' must be a valid finite number.");
        }
        if (attY < -360.0 || attY > 360.0) {
            throw new InvalidTelemetryException(String.format(
                    "Field 'attitudeControlErrorY' exceeds physical angular limits (%.2f°). Acceptable range: -360.0° to 360.0°.", attY));
        }

        // 4. Radiation Exposure Level (0.0 to 100,000.0 µSv/h)
        double rad = telemetry.getRadiationExposureLevel();
        if (Double.isNaN(rad) || Double.isInfinite(rad)) {
            throw new InvalidTelemetryException("Field 'radiationExposureLevel' must be a valid finite number.");
        }
        if (rad < 0.0 || rad > 100000.0) {
            throw new InvalidTelemetryException(String.format(
                    "Field 'radiationExposureLevel' cannot be negative or unrealistically huge (%.2f µSv/h). Acceptable range: 0.0 to 100,000.0 µSv/h.", rad));
        }

        log.debug("Telemetry validated successfully for satellite: {}", telemetry.getSatelliteId());
    }
}
