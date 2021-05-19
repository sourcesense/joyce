package com.sourcesense.nile.core.exception;

import com.sourcesense.nile.core.enumeration.NotificationEvent;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.springframework.http.HttpStatus;

//Todo: da cambiare
@Getter
public class ProcessingException extends NotificationException{

    private final HttpStatus httpStatus;

    public ProcessingException(
            String message,
            String rawUri,
            String contentUri,
            NotificationEvent event,
            HttpStatus httpStatus) {

        super(message, rawUri, contentUri, event);
        this.httpStatus = httpStatus;
    }
}
