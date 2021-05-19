package com.sourcesense.nile.core.aspect;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sourcesense.nile.core.annotation.*;
import com.sourcesense.nile.core.enumeration.NotificationEvent;
import com.sourcesense.nile.core.exception.NotifiedException;
import com.sourcesense.nile.core.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

@Aspect
@Component
@RequiredArgsConstructor
public class NotificationAspect implements MethodAspect {

    private final NotificationService notificationService;

    @Around("@annotation(com.sourcesense.nile.core.annotation.Notify)")
    public Object notify(ProceedingJoinPoint joinPoint) {

        Method method = this.computeMethod(joinPoint);
        Parameter[] params = method.getParameters();

        String rawUri = this.computeUri(params, joinPoint.getArgs(), RawUri.class);
        String contentUri = this.computeUri(params, joinPoint.getArgs(), ContentUri.class);
        JsonNode eventPayload = this.computeNode(params, joinPoint.getArgs(), EventPayload.class);
        JsonNode eventMetadata = this.computeNode(params, joinPoint.getArgs(), EventMetadata.class);

        return this.notify(joinPoint, method, rawUri, contentUri, eventPayload, eventMetadata);
    }

    private Object notify(
            ProceedingJoinPoint joinPoint,
            Method method,
            String rawUri,
            String contentUri,
            JsonNode eventPayload,
            JsonNode eventMetadata) {

        try {
            Object result = joinPoint.proceed();
            this.computeNotificationEvent(method, Notify::successEvent).ifPresent(
                    successEvent -> notificationService.ok(rawUri, contentUri, successEvent, eventPayload, eventMetadata)
            );

            return result;

        } catch (Throwable exception) {
            if (!NotifiedException.class.equals(exception.getClass())) {
                this.computeNotificationEvent(method, Notify::failureEvent).ifPresent(
                        failureEvent -> notificationService.ko(rawUri, contentUri, failureEvent, eventPayload, eventMetadata)
                );
                throw new NotifiedException(exception);
            }
            throw new RuntimeException(exception);
        }
    }

    private Optional<NotificationEvent> computeNotificationEvent(
            Method method,
            Function<Notify, NotificationEvent> eventProvider) {

        return Optional.ofNullable(method)
                .map(annotatedMethod -> annotatedMethod.getAnnotation(Notify.class))
                .map(eventProvider)
                .filter(Predicate.not(NotificationEvent.NONE::equals));
    }

    //Uri can of type NileURI or String
    private <A extends Annotation> String computeUri(
            Parameter[] params,
            Object[] paramsValues,
            Class<A> annotationClass) {

        return this.computeAnnotatedParamValue(params, paramsValues, annotationClass, Object.class)
                .map(Object::toString)
                .orElse("");
    }

    private <A extends Annotation> JsonNode computeNode(
            Parameter[] params,
            Object[] paramsValues,
            Class<A> annotationClass) {

        return this.computeAnnotatedParamValue(params, paramsValues, annotationClass, JsonNode.class)
                .orElse(new ObjectNode(JsonNodeFactory.instance));
    }
}
