package com.stellarintel.exception;

import com.stellarintel.dto.TelemetryIngestResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Centralized exception handler for REST API endpoints.
 * Ensures graceful error responses and prevents application crashes on malformed input.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(InvalidTelemetryException.class)
    public ResponseEntity<TelemetryIngestResponse> handleInvalidTelemetry(InvalidTelemetryException ex) {
        log.warn("Telemetry validation failed: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(TelemetryIngestResponse.error("Validation Error: " + ex.getMessage()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<TelemetryIngestResponse> handleMalformedJson(HttpMessageNotReadableException ex) {
        log.warn("Malformed JSON received: {}", ex.getMessage());
        String msg = ex.getMessage();
        String safeDetail;
        if (msg != null && msg.contains("Required request body is missing")) {
            safeDetail = "Request body is missing. Please provide a valid JSON telemetry object.";
        } else if (ex.getMostSpecificCause() != null) {
            safeDetail = ex.getMostSpecificCause().getMessage();
            // Remove noisy Jackson internal stream details if present
            int atIndex = safeDetail.indexOf(" at [Source:");
            if (atIndex > 0) {
                safeDetail = safeDetail.substring(0, atIndex).trim();
            }
        } else {
            safeDetail = "Syntax error in JSON structure.";
        }
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(TelemetryIngestResponse.error("Malformed JSON payload: Please verify telemetry JSON syntax. (" + safeDetail + ")"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<TelemetryIngestResponse> handleGeneralException(Exception ex) {
        log.error("Unexpected error occurred while processing request: ", ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(TelemetryIngestResponse.error("Internal Server Error: An unexpected error occurred while processing telemetry. Please verify input data."));
    }
}
