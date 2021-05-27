package com.sourcesense.nile.core.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * This annotation marks the parameter of a method annotated with
 * {@link com.sourcesense.nile.core.annotation.Notify} that will
 * contain the content uri used to send a notification to kafka
 * notification topic in {@link com.sourcesense.nile.core.aspect.NotificationAspect}
 */
@Target(PARAMETER)
@Retention(RUNTIME)
public @interface ContentUri {
}
