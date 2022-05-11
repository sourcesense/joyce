package com.sourcesense.joyce.core.exception;

import com.sourcesense.joyce.core.enumeration.NotificationEvent;
import lombok.Getter;
import org.springframework.http.HttpStatus;

//Todo: da cambiare
@Getter
public class ProcessingException extends NotificationException{

    private final HttpStatus httpStatus;

    public ProcessingException(
            String message,
            String contentURI,
            String sourceURI,
            NotificationEvent event,
            HttpStatus httpStatus) {

        super(message, contentURI, sourceURI, event);
        this.httpStatus = httpStatus;
    }
}
