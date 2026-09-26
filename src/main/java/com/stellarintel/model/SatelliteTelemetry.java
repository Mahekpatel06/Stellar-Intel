package com.stellarintel.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * SatelliteTelemetry represents raw numerical telemetry transmitted by an orbiting satellite.
 *
 * In accordance with Phase 1 design principles:
 * - This model encapsulates the essential numerical telemetry attributes.
 * - Subsystem status is NOT accepted as a trusted input from the client payload.
 *   System status will be derived dynamically by rule-based analysis in Phase 2.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SatelliteTelemetry {

    /**
     * Unique identifier for the spacecraft (e.g., "ASTRO-SAT-01", "ORBITER-7").
     */
    private String satelliteId;

    /**
     * ISO-8601 formatted timestamp of telemetry measurement (e.g., "2026-09-25T12:00:00Z").
     */
    private String timestamp;

    /**
     * Battery pack temperature measured in degrees Celsius (°C).
     * Typical nominal operating range: -10.0°C to +35.0°C.
     */
    private double batteryTemperatureCelsius;

    /**
     * Solar array electrical bus output potential measured in Volts (V).
     * Typical nominal range: 26.0V to 34.0V.
     */
    private double solarPanelVoltage;

    /**
     * Attitude Determination & Control System (ADCS) angular pointing error on the X-axis (degrees).
     * Typical nominal threshold: |error| <= 0.05°.
     */
    private double attitudeControlErrorX;

    /**
     * Attitude Determination & Control System (ADCS) angular pointing error on the Y-axis (degrees).
     * Typical nominal threshold: |error| <= 0.05°.
     */
    private double attitudeControlErrorY;

    /**
     * Ionizing radiation sensor reading measured in millirad/hour or microsieverts/hour (µSv/h).
     * Nominal deep-space background: 0.1 to 1.5 µSv/h.
     */
    private double radiationExposureLevel;

    public SatelliteTelemetry() {
    }

    public SatelliteTelemetry(String satelliteId, String timestamp, double batteryTemperatureCelsius,
                              double solarPanelVoltage, double attitudeControlErrorX,
                              double attitudeControlErrorY, double radiationExposureLevel) {
        this.satelliteId = satelliteId;
        this.timestamp = timestamp;
        this.batteryTemperatureCelsius = batteryTemperatureCelsius;
        this.solarPanelVoltage = solarPanelVoltage;
        this.attitudeControlErrorX = attitudeControlErrorX;
        this.attitudeControlErrorY = attitudeControlErrorY;
        this.radiationExposureLevel = radiationExposureLevel;
    }

    // Static Builder pattern implementation to guarantee compilation across all JDK versions
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String satelliteId;
        private String timestamp;
        private double batteryTemperatureCelsius;
        private double solarPanelVoltage;
        private double attitudeControlErrorX;
        private double attitudeControlErrorY;
        private double radiationExposureLevel;

        public Builder satelliteId(String satelliteId) {
            this.satelliteId = satelliteId;
            return this;
        }

        public Builder timestamp(String timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Builder batteryTemperatureCelsius(double batteryTemperatureCelsius) {
            this.batteryTemperatureCelsius = batteryTemperatureCelsius;
            return this;
        }

        public Builder solarPanelVoltage(double solarPanelVoltage) {
            this.solarPanelVoltage = solarPanelVoltage;
            return this;
        }

        public Builder attitudeControlErrorX(double attitudeControlErrorX) {
            this.attitudeControlErrorX = attitudeControlErrorX;
            return this;
        }

        public Builder attitudeControlErrorY(double attitudeControlErrorY) {
            this.attitudeControlErrorY = attitudeControlErrorY;
            return this;
        }

        public Builder radiationExposureLevel(double radiationExposureLevel) {
            this.radiationExposureLevel = radiationExposureLevel;
            return this;
        }

        public SatelliteTelemetry build() {
            return new SatelliteTelemetry(satelliteId, timestamp, batteryTemperatureCelsius,
                    solarPanelVoltage, attitudeControlErrorX, attitudeControlErrorY, radiationExposureLevel);
        }
    }

    // Explicit getters and setters to ensure reliability across all JDK compilation environments
    public String getSatelliteId() {
        return satelliteId;
    }

    public void setSatelliteId(String satelliteId) {
        this.satelliteId = satelliteId;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public double getBatteryTemperatureCelsius() {
        return batteryTemperatureCelsius;
    }

    public void setBatteryTemperatureCelsius(double batteryTemperatureCelsius) {
        this.batteryTemperatureCelsius = batteryTemperatureCelsius;
    }

    public double getSolarPanelVoltage() {
        return solarPanelVoltage;
    }

    public void setSolarPanelVoltage(double solarPanelVoltage) {
        this.solarPanelVoltage = solarPanelVoltage;
    }

    public double getAttitudeControlErrorX() {
        return attitudeControlErrorX;
    }

    public void setAttitudeControlErrorX(double attitudeControlErrorX) {
        this.attitudeControlErrorX = attitudeControlErrorX;
    }

    public double getAttitudeControlErrorY() {
        return attitudeControlErrorY;
    }

    public void setAttitudeControlErrorY(double attitudeControlErrorY) {
        this.attitudeControlErrorY = attitudeControlErrorY;
    }

    public double getRadiationExposureLevel() {
        return radiationExposureLevel;
    }

    public void setRadiationExposureLevel(double radiationExposureLevel) {
        this.radiationExposureLevel = radiationExposureLevel;
    }

    @Override
    public String toString() {
        return "SatelliteTelemetry{" +
                "satelliteId='" + satelliteId + '\'' +
                ", timestamp='" + timestamp + '\'' +
                ", batteryTemperatureCelsius=" + batteryTemperatureCelsius +
                ", solarPanelVoltage=" + solarPanelVoltage +
                ", attitudeControlErrorX=" + attitudeControlErrorX +
                ", attitudeControlErrorY=" + attitudeControlErrorY +
                ", radiationExposureLevel=" + radiationExposureLevel +
                '}';
    }
}
