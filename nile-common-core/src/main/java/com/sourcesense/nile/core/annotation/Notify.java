package com.sourcesense.nile.core.annotation;

import com.sourcesense.nile.core.enumeration.NotificationEvent;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * This annotation marks a method and activates the aspect
 * {@link com.sourcesense.nile.core.aspect.NotificationAspect}
 * that will send a notification to kafka notification topic using
 * the data retrieved by the method parameters annotated with:
 * {@link com.sourcesense.nile.core.annotation.RawUri}
 * {@link com.sourcesense.nile.core.annotation.ContentUri}
 * {@link com.sourcesense.nile.core.annotation.EventPayload}
 * {@link com.sourcesense.nile.core.annotation.EventMetadata}
 */
@Target(METHOD)
@Retention(RUNTIME)
public @interface Notify {

    NotificationEvent successEvent() default NotificationEvent.NONE;
    NotificationEvent failureEvent() default NotificationEvent.NONE;
}
