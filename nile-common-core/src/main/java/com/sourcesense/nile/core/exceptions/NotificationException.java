package com.sourcesense.nile.core.exceptions;

import com.sourcesense.nile.core.enumeration.NotificationEvent;
import lombok.Getter;

@Getter
public class NotificationException extends RuntimeException{

    protected NotificationEvent event;

    public NotificationException(String message) {
        super(message);
    }

    public NotificationException(String message, NotificationEvent event) {
        super(message);
        this.event = event;
    }
}
