package org.assignment.exception;

import lombok.Getter;

/**
 * Exception thrown when there is an invalid models-related request.
 */
@Getter
public class InvalidModelsControllerException extends RuntimeException {

    private final AppErrorCode errorCode;
    private final String debugMessage;

    public InvalidModelsControllerException(AppErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.debugMessage = null;
    }

    public InvalidModelsControllerException(AppErrorCode errorCode, String debugMessage) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.debugMessage = debugMessage;
    }

}
