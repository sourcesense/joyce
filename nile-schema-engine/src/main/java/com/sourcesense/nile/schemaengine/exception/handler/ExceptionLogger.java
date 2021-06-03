package com.sourcesense.nile.schemaengine.exception.handler;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;

@Slf4j
public class ExceptionLogger {

    public static void logException(String message, Exception exception) {
        log.error(message);
        if(log.isDebugEnabled()) {
            log.debug(ExceptionUtils.getStackTrace(exception));
        }
    }
}
