package com.sourcesense.joyce.core.exception.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomExceptionHandler {

    public void handleException(Exception exception) {
        log.error(exception.getMessage());
        if(log.isDebugEnabled()) {
            log.debug(ExceptionUtils.getStackTrace(exception));
        }
    }
}
