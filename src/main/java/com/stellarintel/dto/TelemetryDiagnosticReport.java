package com.stellarintel.dto;

import com.stellarintel.model.TelemetryAnalysisResult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Unified response DTO combining the Phase 2 deterministic analysis
 * and the Phase 3 Generative AI technical diagnostic.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TelemetryDiagnosticReport {

    /**
     * Raw telemetry payload associated with this diagnostic report (Phase 6).
     */
    private com.stellarintel.model.SatelliteTelemetry telemetry;

    /**
     * Authoritative deterministic numerical analysis (Ground Truth from Phase 2).
     */
    private TelemetryAnalysisResult analysis;

    /**
     * Generative AI diagnostic interpretation (Phase 3).
     */
    private GenAiDiagnosticResponse diagnostic;

    /**
     * Timestamp of report generation.
     */
    private String timestamp;

    public TelemetryDiagnosticReport() {
    }

    public TelemetryDiagnosticReport(TelemetryAnalysisResult analysis, GenAiDiagnosticResponse diagnostic, String timestamp) {
        this.telemetry = null;
        this.analysis = analysis;
        this.diagnostic = diagnostic;
        this.timestamp = timestamp;
    }

    public TelemetryDiagnosticReport(com.stellarintel.model.SatelliteTelemetry telemetry,
                                     TelemetryAnalysisResult analysis,
                                     GenAiDiagnosticResponse diagnostic,
                                     String timestamp) {
        this.telemetry = telemetry;
        this.analysis = analysis;
        this.diagnostic = diagnostic;
        this.timestamp = timestamp;
    }

    public static TelemetryDiagnosticReport of(TelemetryAnalysisResult analysis, GenAiDiagnosticResponse diagnostic) {
        return new TelemetryDiagnosticReport(analysis, diagnostic, Instant.now().toString());
    }

    public static TelemetryDiagnosticReport of(com.stellarintel.model.SatelliteTelemetry telemetry,
                                               TelemetryAnalysisResult analysis,
                                               GenAiDiagnosticResponse diagnostic) {
        return new TelemetryDiagnosticReport(telemetry, analysis, diagnostic, Instant.now().toString());
    }

    public com.stellarintel.model.SatelliteTelemetry getTelemetry() {
        return telemetry;
    }

    public void setTelemetry(com.stellarintel.model.SatelliteTelemetry telemetry) {
        this.telemetry = telemetry;
    }

    public TelemetryAnalysisResult getAnalysis() {
        return analysis;
    }

    public void setAnalysis(TelemetryAnalysisResult analysis) {
        this.analysis = analysis;
    }

    public GenAiDiagnosticResponse getDiagnostic() {
        return diagnostic;
    }

    public void setDiagnostic(GenAiDiagnosticResponse diagnostic) {
        this.diagnostic = diagnostic;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
