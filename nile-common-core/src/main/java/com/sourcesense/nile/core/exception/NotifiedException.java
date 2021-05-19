package com.sourcesense.nile.core.exception;


public class NotifiedException extends RuntimeException {

    public NotifiedException(Throwable exception) {
        super(exception);
    }
}
