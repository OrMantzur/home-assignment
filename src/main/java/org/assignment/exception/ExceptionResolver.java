package org.assignment.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.time.LocalDateTime;

/**
 * Global exception handler to convert exceptions into standardized API error responses.
 */
@Slf4j
@ControllerAdvice
public class ExceptionResolver {

    /**
     * Handle InvalidModelsControllerException and convert it to an API error response.
     *
     * @param ex
     * @return
     */
    @ExceptionHandler(InvalidModelsControllerException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidModelException(InvalidModelsControllerException ex) {
        log.error("Business Error: {}", ex.getMessage());
        return buildResponse(ex.getErrorCode(), ex.getDebugMessage());
    }

    /**
     * Handle malformed JSON input errors.
     *
     * @param ex
     * @return
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleJsonErrors(HttpMessageNotReadableException ex) {
        log.error("JSON Error: {}", ex.getMessage());
        return buildResponse(AppErrorCode.INVALID_JSON_FORMAT, ex.getMessage());
    }

    /**
     * Handle unsupported HTTP methods.
     *
     * @param ex
     * @return
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        return buildResponse(AppErrorCode.METHOD_NOT_ALLOWED, ex.getMessage());
    }

    /**
     * Handle requests to non-existent endpoints.
     *
     * @param ex
     * @return
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(NoHandlerFoundException ex) {
        String debugInfo = String.format("Method: %s, URL: %s", ex.getHttpMethod(), ex.getRequestURL());
        return buildResponse(AppErrorCode.RESOURCE_NOT_FOUND, debugInfo);
    }

    /**
     * Handle all other uncaught exceptions.
     *
     * @param ex
     * @return
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGeneralErrors(Exception ex) {
        log.error("Unexpected System Error", ex); // Print stack trace here
        // We do NOT expose the raw stack trace to the user, only "Internal Error"
        return buildResponse(AppErrorCode.INTERNAL_ERROR, ex.getMessage());
    }

    /**
     * Helper method to build the standardized API error response.
     *
     * @param code
     * @param debugInfo
     * @return
     */
    private ResponseEntity<ApiErrorResponse> buildResponse(AppErrorCode code, String debugInfo) {
        ApiErrorResponse response = ApiErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .errorCode(code.getCode())
                .message(code.getMessage())
                .debugMessage(debugInfo)
                .build();

        return new ResponseEntity<>(response, code.getStatus());
    }

}