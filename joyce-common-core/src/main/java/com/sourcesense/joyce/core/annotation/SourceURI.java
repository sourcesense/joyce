package com.sourcesense.joyce.core.annotation;

import com.sourcesense.joyce.core.aspect.NotificationAspect;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * This annotation marks the parameter of a method annotated with
 * {@link Notify} that will
 * contain the raw uri used to send a notification to kafka
 * notification topic in {@link NotificationAspect}
 */
@Target(PARAMETER)
@Retention(RUNTIME)
public @interface SourceURI {

}
