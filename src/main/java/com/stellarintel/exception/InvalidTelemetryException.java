package com.stellarintel.exception;

/**
 * Exception thrown when ingested telemetry data violates structural or sanity requirements.
 */
public class InvalidTelemetryException extends RuntimeException {

    public InvalidTelemetryException(String message) {
        super(message);
    }

    public InvalidTelemetryException(String message, Throwable cause) {
        super(message, cause);
    }
}
