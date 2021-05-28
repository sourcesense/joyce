package com.sourcesense.joyce.core.annotation;

import com.sourcesense.joyce.core.aspect.NotificationAspect;
import com.sourcesense.joyce.core.enumeration.NotificationEvent;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * This annotation marks a method and activates the aspect
 * {@link NotificationAspect}
 * that will send a notification to kafka notification topic using
 * the data retrieved by the method parameters annotated with:
 * {@link RawUri}
 * {@link ContentUri}
 * {@link EventPayload}
 * {@link EventMetadata}
 */
@Target(METHOD)
@Retention(RUNTIME)
public @interface Notify {

    NotificationEvent successEvent() default NotificationEvent.NONE;
    NotificationEvent failureEvent() default NotificationEvent.NONE;
}
