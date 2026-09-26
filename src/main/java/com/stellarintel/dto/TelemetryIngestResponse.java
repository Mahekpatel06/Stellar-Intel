package com.stellarintel.dto;

import com.stellarintel.model.SatelliteTelemetry;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Data Transfer Object representing the outcome of telemetry ingestion/validation.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TelemetryIngestResponse {

    private boolean success;
    private String message;
    private SatelliteTelemetry telemetry;
    private String processedAt;

    public TelemetryIngestResponse() {
    }

    public TelemetryIngestResponse(boolean success, String message, SatelliteTelemetry telemetry, String processedAt) {
        this.success = success;
        this.message = message;
        this.telemetry = telemetry;
        this.processedAt = processedAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private boolean success;
        private String message;
        private SatelliteTelemetry telemetry;
        private String processedAt;

        public Builder success(boolean success) {
            this.success = success;
            return this;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public Builder telemetry(SatelliteTelemetry telemetry) {
            this.telemetry = telemetry;
            return this;
        }

        public Builder processedAt(String processedAt) {
            this.processedAt = processedAt;
            return this;
        }

        public TelemetryIngestResponse build() {
            return new TelemetryIngestResponse(success, message, telemetry, processedAt);
        }
    }

    public static TelemetryIngestResponse ok(String message, SatelliteTelemetry telemetry) {
        return TelemetryIngestResponse.builder()
                .success(true)
                .message(message)
                .telemetry(telemetry)
                .processedAt(Instant.now().toString())
                .build();
    }

    public static TelemetryIngestResponse error(String message) {
        return TelemetryIngestResponse.builder()
                .success(false)
                .message(message)
                .telemetry(null)
                .processedAt(Instant.now().toString())
                .build();
    }

    // Explicit getters and setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public SatelliteTelemetry getTelemetry() {
        return telemetry;
    }

    public void setTelemetry(SatelliteTelemetry telemetry) {
        this.telemetry = telemetry;
    }

    public String getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(String processedAt) {
        this.processedAt = processedAt;
    }
}
