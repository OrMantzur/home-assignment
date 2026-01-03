package org.assignment.exception;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Standard structure for API error responses.
 */
@Data
@Builder
public class ApiErrorResponse {
    private LocalDateTime timestamp;
    private String errorCode;
    private String message;
    private String debugMessage;
}