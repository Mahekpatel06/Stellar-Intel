package com.stellarintel.model;

/**
 * Severity level assigned to a detected telemetry anomaly.
 */
public enum AnomalySeverity {
    /**
     * Parameter exceeds nominal threshold but remains within controllable limits.
     */
    WARNING,

    /**
     * Parameter exceeds critical threshold posing a risk of subsystem degradation or vehicle failure.
     */
    CRITICAL
}
