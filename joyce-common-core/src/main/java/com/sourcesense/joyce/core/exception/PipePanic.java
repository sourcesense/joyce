package com.sourcesense.joyce.core.exception;

import com.sourcesense.joyce.core.enumeration.NotificationEvent;
import lombok.Getter;

/**
 * DON'T PANIC
 * <p>
 * questa eccezione ha lo scopo di bloccare una pipeline in corso
 */

@Getter
public class PipePanic extends RuntimeException {

    private NotificationEvent event;

    public PipePanic(String msg) {
        super(msg);
    }

    public PipePanic(String msg, NotificationEvent event) {
        super(msg);
        this.event = event;
    }
}
