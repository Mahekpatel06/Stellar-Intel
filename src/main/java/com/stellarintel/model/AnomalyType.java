package com.stellarintel.model;

/**
 * Classification category of a detected satellite telemetry anomaly.
 */
public enum AnomalyType {
    /**
     * Battery or component temperature exceeds safe thermal operating envelope.
     */
    THERMAL_ANOMALY,

    /**
     * Dosimeter reading indicates dangerous flux of ionizing radiation.
     */
    HIGH_RADIATION,

    /**
     * ADCS angular pointing error magnitude exceeds fine pointing tolerance.
     */
    ATTITUDE_CONTROL_ANOMALY,

    /**
     * Electrical Power System (EPS) voltage is below nominal battery bus threshold.
     */
    VOLTAGE_UNDER_RANGE,

    /**
     * Electrical Power System (EPS) voltage exceeds over-voltage regulator threshold.
     */
    VOLTAGE_OVER_RANGE
}
