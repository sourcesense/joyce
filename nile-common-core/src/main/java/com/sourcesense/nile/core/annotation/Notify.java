package com.sourcesense.nile.core.annotation;

import com.sourcesense.nile.core.enumeration.NotificationEvent;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target(METHOD)
@Retention(RUNTIME)
public @interface Notify {

    NotificationEvent successEvent() default NotificationEvent.NONE;
    NotificationEvent failureEvent() default NotificationEvent.NONE;
}
