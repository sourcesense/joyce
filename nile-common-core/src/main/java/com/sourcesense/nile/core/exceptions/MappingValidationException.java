package com.sourcesense.nile.core.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class MappingValidationException extends RuntimeException{

    private static final String ERROR_MESSAGE = "There has been an error validating a mapping, error message is: ";
    private HttpStatus httpStatus;

    public MappingValidationException(String message) {
        super(ERROR_MESSAGE + message);
    }

    public MappingValidationException(String message, HttpStatus httpStatus) {
        super(ERROR_MESSAGE + message);
        this.httpStatus = httpStatus;
    }
}
