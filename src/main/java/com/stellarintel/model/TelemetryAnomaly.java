package com.stellarintel.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Encapsulates an individual detected telemetry anomaly with its comparison metrics,
 * severity classification, and human-readable explanation.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TelemetryAnomaly {

    /**
     * Human-readable subsystem telemetry parameter name (e.g., "Battery Temperature").
     */
    private String parameter;

    /**
     * Measured or computed numerical value from the telemetry stream.
     */
    private double observedValue;

    /**
     * Textual representation of the engineering threshold limit (e.g., "<= 45.0 °C", "<= 0.050°").
     */
    private String threshold;

    /**
     * Classified severity of this specific anomaly (WARNING or CRITICAL).
     */
    private AnomalySeverity severity;

    /**
     * Structured classification type of the anomaly.
     */
    private AnomalyType anomalyType;

    /**
     * Clear deterministic explanation detailing why the threshold comparison was flagged.
     */
    private String explanation;

    public TelemetryAnomaly() {
    }

    public TelemetryAnomaly(String parameter, double observedValue, String threshold,
                            AnomalySeverity severity, AnomalyType anomalyType, String explanation) {
        this.parameter = parameter;
        this.observedValue = observedValue;
        this.threshold = threshold;
        this.severity = severity;
        this.anomalyType = anomalyType;
        this.explanation = explanation;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String parameter;
        private double observedValue;
        private String threshold;
        private AnomalySeverity severity;
        private AnomalyType anomalyType;
        private String explanation;

        public Builder parameter(String parameter) {
            this.parameter = parameter;
            return this;
        }

        public Builder observedValue(double observedValue) {
            this.observedValue = observedValue;
            return this;
        }

        public Builder threshold(String threshold) {
            this.threshold = threshold;
            return this;
        }

        public Builder severity(AnomalySeverity severity) {
            this.severity = severity;
            return this;
        }

        public Builder anomalyType(AnomalyType anomalyType) {
            this.anomalyType = anomalyType;
            return this;
        }

        public Builder explanation(String explanation) {
            this.explanation = explanation;
            return this;
        }

        public TelemetryAnomaly build() {
            return new TelemetryAnomaly(parameter, observedValue, threshold, severity, anomalyType, explanation);
        }
    }

    // Explicit getters and setters
    public String getParameter() {
        return parameter;
    }

    public void setParameter(String parameter) {
        this.parameter = parameter;
    }

    public double getObservedValue() {
        return observedValue;
    }

    public void setObservedValue(double observedValue) {
        this.observedValue = observedValue;
    }

    public String getThreshold() {
        return threshold;
    }

    public void setThreshold(String threshold) {
        this.threshold = threshold;
    }

    public AnomalySeverity getSeverity() {
        return severity;
    }

    public void setSeverity(AnomalySeverity severity) {
        this.severity = severity;
    }

    public AnomalyType getAnomalyType() {
        return anomalyType;
    }

    public void setAnomalyType(AnomalyType anomalyType) {
        this.anomalyType = anomalyType;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }
}
