package com.sourcesense.nile.core.exception.handler;

import com.jayway.jsonpath.PathNotFoundException;
import com.sourcesense.nile.core.dto.ApiError;
import com.sourcesense.nile.core.exception.*;
import com.sourcesense.nile.schemaengine.exceptions.InvalidSchemaException;
import com.sourcesense.nile.schemaengine.exceptions.SchemaIsNotValidException;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.common.KafkaException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;

import java.util.Objects;


@RequiredArgsConstructor
@ControllerAdvice(basePackages = {
        "com.sourcesense.nile.core",
        "com.sourcesense.nile.connectorcore",
        "com.sourcesense.nile.connector",
        "com.sourcesense.nile.importcore",

})
public class ApiExceptionHandler {

    private final CustomExceptionHandler exceptionHandler;

    @ExceptionHandler(value = InvalidSchemaException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ApiError handler(InvalidSchemaException exception, WebRequest request) {
        return new ApiError(exception.getMessage(), exception.getClass().getCanonicalName());
    }

    @ExceptionHandler(value = InvalidMetadataException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ApiError handler(InvalidMetadataException exception, WebRequest request) {
        return new ApiError(exception.getMessage(), exception.getClass().getCanonicalName());
    }

    @ExceptionHandler(value = PathNotFoundException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public ApiError handler(PathNotFoundException exception, WebRequest request) {
        return new ApiError(exception.getMessage(), exception.getClass().getCanonicalName());
    }

    @ExceptionHandler(value = KafkaException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public ApiError handler(KafkaException exception, WebRequest request) {
        return new ApiError(exception.getMessage(), exception.getClass().getCanonicalName());
    }

    @ExceptionHandler(value = SchemaNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ResponseBody
    public ApiError handler(SchemaNotFoundException exception, WebRequest request) {
        return new ApiError(exception.getMessage(), exception.getClass().getCanonicalName());
    }

    @ExceptionHandler(value = SchemaIsNotValidException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public ApiError handler(SchemaIsNotValidException exception, WebRequest request) {
        return new ApiError(exception.getMessage(), exception.getClass().getCanonicalName());
    }

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
