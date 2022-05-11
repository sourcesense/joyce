package com.sourcesense.joyce.core.exception;

import com.sourcesense.joyce.core.enumeration.NotificationEvent;
import lombok.Builder;
import lombok.Getter;

@Getter
public class NotificationException extends RuntimeException{

    protected final String contentURI;
    protected final String sourceURI;
    protected final NotificationEvent event;

    @Builder
    public NotificationException(
            String message,
            String contentURI,
            String sourceURI,
            NotificationEvent event) {

        super(message);
        this.contentURI = contentURI;
        this.sourceURI = sourceURI;
        this.event = event;
    }
}
