package com.sourcesense.nile.core.exceptions.handler;

import com.sourcesense.nile.core.exceptions.NotificationException;
import com.sourcesense.nile.core.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Log4j2
@Component
@RequiredArgsConstructor
public class CustomExceptionHandler {

    private final NotificationService notificationService;

    public void handleNotificationException(NotificationException exception) {
        this.handleException(exception);
        this.sendNotification(exception);
    }

    public void handleException(Exception exception) {
        log.error(exception.getMessage());
        if(log.isDebugEnabled()) {
            log.debug(ExceptionUtils.getStackTrace(exception));
        }
    }

    private void sendNotification(NotificationException exception) {
        if(Objects.nonNull(exception.getEvent())) {
            notificationService.ko(StringUtils.EMPTY, exception.getEvent(), exception.getMessage());
        }
    }
}
