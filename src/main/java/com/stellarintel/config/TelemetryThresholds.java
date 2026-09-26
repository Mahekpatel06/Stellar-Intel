package com.stellarintel.config;

/**
 * Centralized simulated engineering threshold definitions for StellarIntel telemetry analysis.
 *
 * IMPORTANT DISCLAIMER:
 * These thresholds are synthetic values established specifically for this academic simulation project.
 * They do not claim to represent official NASA, ESA, or commercial spacecraft operating limits.
 */
public final class TelemetryThresholds {

    private TelemetryThresholds() {
        // Utility / Constants class
    }

    // =========================================================================
    // 1. THERMAL ENVELOPE (Battery Pack Temperature in °C)
    // =========================================================================
    /** Nominal threshold: <= 45.0°C. Values > 45.0°C trigger a THERMAL_ANOMALY. */
    public static final double BATTERY_TEMP_WARNING_CELSIUS = 45.0;
    /** Critical threshold: > 50.0°C represents severe thermal runaway risk. */
    public static final double BATTERY_TEMP_CRITICAL_CELSIUS = 50.0;

    // =========================================================================
    // 2. RADIATION DOSIMETRY (Ionizing Radiation Flux in µSv/h)
    // =========================================================================
    /** Nominal threshold: <= 5.0 µSv/h. Values > 5.0 µSv/h trigger a HIGH_RADIATION anomaly. */
    public static final double RADIATION_WARNING_LEVEL = 5.0;
    /** Critical threshold: >= 8.0 µSv/h represents severe radiation belt or solar storm flux. */
    public static final double RADIATION_CRITICAL_LEVEL = 8.0;

    // =========================================================================
    // 3. ATTITUDE DETERMINATION & CONTROL SYSTEM (ADCS Error Magnitude in Degrees)
    // =========================================================================
    /**
     * Calculated magnitude: sqrt(errorX² + errorY²).
     * Nominal threshold: <= 0.05°. Values > 0.05° trigger an ATTITUDE_CONTROL_ANOMALY.
     */
    public static final double ATTITUDE_ERROR_MAGNITUDE_WARNING_DEG = 0.05;
    /** Critical threshold: > 0.50° represents major pointing loss / tumble hazard. */
    public static final double ATTITUDE_ERROR_MAGNITUDE_CRITICAL_DEG = 0.50;

    // =========================================================================
    // 4. ELECTRICAL POWER SYSTEM (Solar Array Bus Potential in Volts)
    // =========================================================================
    /** Nominal lower voltage bound: 26.0 V. Values < 26.0 V trigger VOLTAGE_UNDER_RANGE. */
    public static final double SOLAR_VOLTAGE_MIN_NOMINAL_VOLTS = 26.0;
    /** Nominal upper voltage bound: 35.0 V. Values > 35.0 V trigger VOLTAGE_OVER_RANGE. */
    public static final double SOLAR_VOLTAGE_MAX_NOMINAL_VOLTS = 35.0;
    /** Critical under-voltage threshold: <= 22.0 V represents deep battery drain. */
    public static final double SOLAR_VOLTAGE_CRITICAL_LOW_VOLTS = 22.0;
    /** Critical over-voltage threshold: >= 38.0 V represents potential regulator failure. */
    public static final double SOLAR_VOLTAGE_CRITICAL_HIGH_VOLTS = 38.0;
}
