package com.sourcesense.joyce.core.exception;

import com.sourcesense.joyce.core.enumeration.NotificationEvent;
import lombok.Builder;
import lombok.Getter;

@Getter
public class NotificationException extends RuntimeException{

    protected final String rawUri;
    protected final String contentUri;
    protected final NotificationEvent event;

    @Builder
    public NotificationException(
            String message,
            String rawUri,
            String contentUri,
            NotificationEvent event) {

        super(message);
        this.rawUri = rawUri;
        this.contentUri = contentUri;
        this.event = event;
    }
}
