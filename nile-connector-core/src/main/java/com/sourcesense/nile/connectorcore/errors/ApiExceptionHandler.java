package com.sourcesense.nile.connectorcore.errors;

import com.sourcesense.nile.core.dto.ApiError;
import com.sourcesense.nile.core.exceptions.DataInfoNotFoundException;
import com.sourcesense.nile.core.exceptions.MappingValidationException;
import com.sourcesense.nile.core.exceptions.ProcessingException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Objects;


@RequiredArgsConstructor
@ControllerAdvice(basePackages = {
        "com.sourcesense.nile.core",
        "com.sourcesense.nile.connectorcore",
        "com.sourcesense.nile.connector"
})
public class ApiExceptionHandler {

    private final CustomExceptionHandler exceptionHandler;

    @ExceptionHandler(MappingValidationException.class)
    ResponseEntity<ApiError> mappingValidationException(MappingValidationException exception) {
        exceptionHandler.handleException(exception);
        return this.getErrorResponse(exception.getMessage(), exception.getHttpStatus());
    }

    @ExceptionHandler(DataInfoNotFoundException.class)
    ResponseEntity<ApiError> dataInfoNotFoundException(DataInfoNotFoundException exception) {
        exceptionHandler.handleException(exception);
        return this.getErrorResponse(exception.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ProcessingException.class)
    ResponseEntity<ApiError> processingException(ProcessingException exception) {
        exceptionHandler.handleNotificationException(exception);
        return this.getErrorResponse(exception.getMessage(), exception.getHttpStatus());
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiError> exception(Exception exception) {
        exceptionHandler.handleException(exception);
        return this.getErrorResponse(exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<ApiError> getErrorResponse(String message, HttpStatus httpStatus) {
        HttpStatus status = Objects.nonNull(httpStatus) ? httpStatus : HttpStatus.INTERNAL_SERVER_ERROR;
        return ResponseEntity.status(status).body(new ApiError(message, status.name()));
    }
}
