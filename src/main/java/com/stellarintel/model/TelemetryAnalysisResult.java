package com.stellarintel.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Encapsulates the complete result of the deterministic telemetry analysis.
 *
 * This structured model forms the baseline factual ground truth that will later
 * be supplied to the GenAI reasoning pipeline in subsequent phases.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TelemetryAnalysisResult {

    /**
     * Unique identifier for the analyzed satellite vehicle.
     */
    private String satelliteId;

    /**
     * Timestamp of the original telemetry packet.
     */
    private String timestamp;

    /**
     * Derived vehicle health status (NOMINAL, WARNING, CRITICAL).
     */
    private HealthStatus overallStatus;

    /**
     * Boolean indicator of whether any threshold violations were detected.
     */
    private boolean anomalyDetected;

    /**
     * Total count of detected anomalies across all monitored subsystems.
     */
    private int anomalyCount;

    /**
     * Detailed collection of individual parameter anomalies.
     */
    private List<TelemetryAnomaly> detectedAnomalies;

    /**
     * Concise human-readable summary derived strictly from deterministic rule logic.
     */
    private String summary;

    /**
     * Deterministic justification explaining why the overall health status was assigned.
     */
    private String statusExplanation;

    /**
     * Timestamp when the deterministic analysis was executed.
     */
    private String analyzedAt;

    public TelemetryAnalysisResult() {
        this.detectedAnomalies = new ArrayList<>();
    }

    public TelemetryAnalysisResult(String satelliteId, String timestamp, HealthStatus overallStatus,
                                   boolean anomalyDetected, int anomalyCount,
                                   List<TelemetryAnomaly> detectedAnomalies, String summary,
                                   String statusExplanation, String analyzedAt) {
        this.satelliteId = satelliteId;
        this.timestamp = timestamp;
        this.overallStatus = overallStatus;
        this.anomalyDetected = anomalyDetected;
        this.anomalyCount = anomalyCount;
        this.detectedAnomalies = detectedAnomalies != null ? detectedAnomalies : new ArrayList<>();
        this.summary = summary;
        this.statusExplanation = statusExplanation;
        this.analyzedAt = analyzedAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String satelliteId;
        private String timestamp;
        private HealthStatus overallStatus;
        private boolean anomalyDetected;
        private int anomalyCount;
        private List<TelemetryAnomaly> detectedAnomalies = new ArrayList<>();
        private String summary;
        private String statusExplanation;
        private String analyzedAt;

        public Builder satelliteId(String satelliteId) {
            this.satelliteId = satelliteId;
            return this;
        }

        public Builder timestamp(String timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Builder overallStatus(HealthStatus overallStatus) {
            this.overallStatus = overallStatus;
            return this;
        }

        public Builder anomalyDetected(boolean anomalyDetected) {
            this.anomalyDetected = anomalyDetected;
            return this;
        }

        public Builder anomalyCount(int anomalyCount) {
            this.anomalyCount = anomalyCount;
            return this;
        }

        public Builder detectedAnomalies(List<TelemetryAnomaly> detectedAnomalies) {
            this.detectedAnomalies = detectedAnomalies != null ? detectedAnomalies : new ArrayList<>();
            return this;
        }

        public Builder summary(String summary) {
            this.summary = summary;
            return this;
        }

        public Builder statusExplanation(String statusExplanation) {
            this.statusExplanation = statusExplanation;
            return this;
        }

        public Builder analyzedAt(String analyzedAt) {
            this.analyzedAt = analyzedAt;
            return this;
        }

        public TelemetryAnalysisResult build() {
            return new TelemetryAnalysisResult(satelliteId, timestamp, overallStatus, anomalyDetected,
                    anomalyCount, detectedAnomalies, summary, statusExplanation, analyzedAt);
        }
    }

    // Explicit getters and setters
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

    public HealthStatus getOverallStatus() {
        return overallStatus;
    }

    public void setOverallStatus(HealthStatus overallStatus) {
        this.overallStatus = overallStatus;
    }

    public boolean isAnomalyDetected() {
        return anomalyDetected;
    }

    public void setAnomalyDetected(boolean anomalyDetected) {
        this.anomalyDetected = anomalyDetected;
    }

    public int getAnomalyCount() {
        return anomalyCount;
    }

    public void setAnomalyCount(int anomalyCount) {
        this.anomalyCount = anomalyCount;
    }

    public List<TelemetryAnomaly> getDetectedAnomalies() {
        return detectedAnomalies;
    }

    public void setDetectedAnomalies(List<TelemetryAnomaly> detectedAnomalies) {
        this.detectedAnomalies = detectedAnomalies != null ? detectedAnomalies : new ArrayList<>();
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getStatusExplanation() {
        return statusExplanation;
    }

    public void setStatusExplanation(String statusExplanation) {
        this.statusExplanation = statusExplanation;
    }

    public String getAnalyzedAt() {
        return analyzedAt;
    }

    public void setAnalyzedAt(String analyzedAt) {
        this.analyzedAt = analyzedAt;
    }
}
