package com.sourcesense.nile.core.errors;

import com.sourcesense.nile.core.enumeration.NotificationEvent;
import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ProcessingException extends NotificationException{

    private HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;

    public ProcessingException(String message) {
        super(message);
    }

    @Builder
    public ProcessingException(
            String message,
            NotificationEvent event,
            HttpStatus httpStatus) {

        super(message, event);
        this.httpStatus = httpStatus;
    }
}
