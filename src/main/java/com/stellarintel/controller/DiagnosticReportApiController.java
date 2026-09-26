package com.stellarintel.controller;

import com.stellarintel.dto.TelemetryDiagnosticReport;
import com.stellarintel.service.DiagnosticReportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Phase 6: REST API Controller for Automated Satellite Diagnostic Report Generation.
 *
 * <p>Exposes the {@code POST /api/reports/diagnostic} endpoint to convert completed
 * telemetry analysis and diagnostic results directly into downloadable PDF reports.
 */
@RestController
@RequestMapping("/api/reports")
public class DiagnosticReportApiController {

    private static final Logger log = LoggerFactory.getLogger(DiagnosticReportApiController.class);

    private final DiagnosticReportService reportService;

    public DiagnosticReportApiController(DiagnosticReportService reportService) {
        this.reportService = reportService;
    }

    /**
     * Generates a downloadable PDF mission diagnostic report from existing telemetry and diagnostic data.
     *
     * @param report the complete diagnostic report structure currently displayed in the Mission Control UI
     * @return PDF binary stream with Content-Disposition header for direct browser download
     */
    @PostMapping(value = "/diagnostic", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> generateDiagnosticReport(@RequestBody(required = false) TelemetryDiagnosticReport report) {
        log.info("Received request to generate satellite mission diagnostic report PDF.");

        if (report == null) {
            report = new TelemetryDiagnosticReport();
        }

        try {
            byte[] pdfBytes = reportService.generateDiagnosticReportPdf(report);

            String satId = "vehicle";
            if (report.getAnalysis() != null && report.getAnalysis().getSatelliteId() != null) {
                satId = report.getAnalysis().getSatelliteId();
            } else if (report.getTelemetry() != null && report.getTelemetry().getSatelliteId() != null) {
                satId = report.getTelemetry().getSatelliteId();
            }

            String sanitizedId = reportService.sanitizeSatelliteId(satId);
            String filename = "stellarintel-diagnostic-report-" + sanitizedId + ".pdf";

            org.springframework.http.ContentDisposition contentDisposition =
                    org.springframework.http.ContentDisposition.attachment()
                            .filename(filename)
                            .build();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDisposition(contentDisposition);
            headers.setContentLength(pdfBytes.length);
            headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

            log.info("Successfully returned PDF report [{}] ({} bytes)", filename, pdfBytes.length);
            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);

        } catch (Exception ex) {
            log.error("Failed to generate diagnostic report PDF: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
