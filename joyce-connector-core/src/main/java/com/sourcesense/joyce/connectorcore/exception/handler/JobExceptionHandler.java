package com.sourcesense.joyce.connectorcore.exception.handler;

import com.sourcesense.joyce.core.exception.handler.CustomException;
import com.sourcesense.joyce.core.exception.handler.CustomExceptionHandler;
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
            case MAPPING_VALIDATION_EXCEPTION:
            case DATA_INFO_NOT_FOUND_EXCEPTION:
            case EXCEPTION:
            default:
                exceptionHandler.handleException(exception);
        }
    }
}
