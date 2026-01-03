package org.assignment.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Enumeration of application-specific error codes, HTTP statuses, and messages.
 */
@Getter
public enum AppErrorCode {

    // General Errors
    INTERNAL_ERROR("ERROR-5000", HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred"),

    // Input/Validation Errors
    INVALID_JSON_FORMAT("ERROR-4001", HttpStatus.BAD_REQUEST, "Malformatted JSON request"),
    MISSING_REQUEST_BODY("ERROR-4002", HttpStatus.BAD_REQUEST, "Request body is missing"),
    METHOD_NOT_ALLOWED("ERROR-4005", HttpStatus.METHOD_NOT_ALLOWED, "HTTP Method not supported for this endpoint"),
    RESOURCE_NOT_FOUND("ERROR-4004", HttpStatus.NOT_FOUND, "The requested resource was not found"),

    // Business Logic Errors
    MODEL_VALIDATION_FAILED("ERROR-4020", HttpStatus.BAD_REQUEST, "Model validation failed"),
    EMPTY_MODEL_LIST("ERROR-4021", HttpStatus.BAD_REQUEST, "The provided model list cannot be empty"),
    MODEL_LIST_TOO_LARGE("ERROR-4023", HttpStatus.PAYLOAD_TOO_LARGE, "Batch size exceeds limit"),
    INVALID_MODEL_SYNTAX("ERROR-4022", HttpStatus.BAD_REQUEST, "The provided model definition is invalid");

    private final String code;
    private final HttpStatus status;
    private final String message;

    AppErrorCode(String code, HttpStatus status, String message) {
        this.code = code;
        this.status = status;
        this.message = message;
    }

}