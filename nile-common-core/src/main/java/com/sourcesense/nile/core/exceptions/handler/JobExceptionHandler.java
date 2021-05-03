package com.sourcesense.nile.core.exceptions.handler;

import com.sourcesense.nile.core.exceptions.ProcessingException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JobExceptionHandler {

    private final CustomExceptionHandler exceptionHandler;

    public void handleGenericException(Exception exception) {
        CustomException customException = CustomException.getCustomException(exception);
        switch (customException) {
            case PROCESSING_EXCEPTION:
            case NOTIFICATION_EXCEPTION:
                exceptionHandler.handleNotificationException((ProcessingException) exception);
                break;
            case MAPPING_VALIDATION_EXCEPTION:
            case DATA_INFO_NOT_FOUND_EXCEPTION:
            case EXCEPTION:
            default:
                exceptionHandler.handleException(exception);
        }
    }
}
