package com.sourcesense.joyce.core.exception.handler;

import com.sourcesense.joyce.core.exception.NotificationException;
import com.sourcesense.joyce.core.exception.ProcessingException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Getter
@RequiredArgsConstructor
public enum CustomException {

    PROCESSING_EXCEPTION(ProcessingException.class),
    NOTIFICATION_EXCEPTION(NotificationException.class),
    EXCEPTION(Exception.class);

    private final Class<? extends Exception> exceptionClass;

    private static final Map<Class<? extends Exception>, CustomException> customExceptionSelector;

    static {
       customExceptionSelector = Map.of(
               PROCESSING_EXCEPTION.exceptionClass, PROCESSING_EXCEPTION,
               NOTIFICATION_EXCEPTION.exceptionClass, NOTIFICATION_EXCEPTION,
               EXCEPTION.exceptionClass, EXCEPTION
       );
    }

    public static <E extends Exception> CustomException getCustomException(E exception) {
        return Optional.of(customExceptionSelector)
                .filter(selector -> Objects.nonNull(exception))
                .map(selector -> selector.get(exception.getClass()))
                .orElse(EXCEPTION);
    }
}
