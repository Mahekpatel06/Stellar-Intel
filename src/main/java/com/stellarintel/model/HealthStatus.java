package com.stellarintel.model;

/**
 * Derived overall health status of a satellite based on deterministic telemetry analysis.
 *
 * NOTE: This status is calculated deterministically by evaluating numerical telemetry against
 * simulated engineering thresholds. It is NOT accepted as a trusted input from the client.
 */
public enum HealthStatus {
    /**
     * All monitored telemetry parameters are within nominal operating limits.
     */
    NOMINAL,

    /**
     * One or more non-critical deviations detected; requires elevated monitoring.
     */
    WARNING,

    /**
     * Critical threshold exceeded or multiple concurrent anomalies detected.
     */
    CRITICAL
}
