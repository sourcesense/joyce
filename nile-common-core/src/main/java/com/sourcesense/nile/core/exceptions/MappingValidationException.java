package com.sourcesense.nile.core.exceptions;

public class MappingValidationException extends RuntimeException{

    public MappingValidationException(String message) {
        super("There has been an error validating a mapping, error message is: " + message);
    }
}
