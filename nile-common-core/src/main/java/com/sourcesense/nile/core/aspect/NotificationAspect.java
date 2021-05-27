package com.sourcesense.nile.core.aspect;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sourcesense.nile.core.annotation.*;
import com.sourcesense.nile.core.enumeration.NotificationEvent;
import com.sourcesense.nile.core.exception.NotifiedException;
import com.sourcesense.nile.core.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.io.ObjectInputFilter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;


/**
 * Aspect that intercept method annotated with
 * {@link com.sourcesense.nile.core.annotation.Notify}
 * It retrieves data from the method parameters annotated with
 * {@link com.sourcesense.nile.core.annotation.RawUri}
 * {@link com.sourcesense.nile.core.annotation.ContentUri}
 * {@link com.sourcesense.nile.core.annotation.EventPayload}
 * {@link com.sourcesense.nile.core.annotation.EventMetadata}
 * and sends notifications to kafka notification topic
 * enriched with the retrieved data.
 * If Notify annotation contains a successEvent it will send the event or
 * else if the Notify annotation contains a failure event and if the intercepted
 * method throws an exception, the aspect catches that exception and sends
 * a message to notification topic with that event.
 */
@Aspect
@Component
@RequiredArgsConstructor
public class NotificationAspect implements MethodAspect {

    private final NotificationService notificationService;

    /**
     * Contains the code that gets executed when a method annotated
     * {@link com.sourcesense.nile.core.annotation.Notify} is called.
     * First it collects data from the annotated parameters, then
     * if sends an event to kafka notification topic
     *
     * @param joinPoint
     * @return
     */
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

    /**
     *
     * Sends an event to kafka notification topic that contains
     * all the data retrieved from the intercepted method parameters
     *
     * @param joinPoint
     * @param method
     * @param rawUri
     * @param contentUri
     * @param eventPayload
     * @param eventMetadata
     * @return
     */
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
                    successEvent -> notificationService.ok(rawUri, contentUri, successEvent, eventMetadata, eventPayload )
            );

            return result;

        } catch (Throwable exception) {
            if (!NotifiedException.class.equals(exception.getClass())) {
                this.computeNotificationEvent(method, Notify::failureEvent).ifPresent(
                        failureEvent -> {
                            ObjectNode meta = JsonNodeFactory.instance.objectNode();
                            if (eventMetadata != null){
                                meta = eventMetadata.deepCopy();
                            }
                            meta.put("error", exception.getMessage());
                            notificationService.ko(rawUri, contentUri, failureEvent, meta, eventPayload);
                        }
                );
                throw new NotifiedException(exception);
            }
            throw new RuntimeException(exception);
        }
    }

    /**
     * Retrieves a success or failure event from the Notify annotation
     *
     * @param method
     * @param eventProvider
     * @return A notification event
     */
    private Optional<NotificationEvent> computeNotificationEvent(
            Method method,
            Function<Notify, NotificationEvent> eventProvider) {

        return Optional.ofNullable(method)
                .map(annotatedMethod -> annotatedMethod.getAnnotation(Notify.class))
                .map(eventProvider)
                .filter(Predicate.not(NotificationEvent.NONE::equals));
    }

    /**
     * Retrieves an uri from a param of the intercepted method
     * annotated with {@link com.sourcesense.nile.core.annotation.RawUri}
     * or {@link com.sourcesense.nile.core.annotation.ContentUri}
     * Uri can of type NileURI or String.
     *
     * @param params
     * @param paramsValues
     * @param annotationClass
     * @param <A>
     * @return Nile uri or string uri
     */
    private <A extends Annotation> String computeUri(
            Parameter[] params,
            Object[] paramsValues,
            Class<A> annotationClass) {

        return this.computeAnnotatedParamValue(params, paramsValues, annotationClass, Object.class)
                .map(Object::toString)
                .orElse("");
    }

    /**
     *
     * Retrieves a json node from a param of the intercepted method
     * annotated with {@link com.sourcesense.nile.core.annotation.EventPayload}
     * or {@link com.sourcesense.nile.core.annotation.EventMetadata}
     *
     * @param params
     * @param paramsValues
     * @param annotationClass
     * @param <A>
     * @return Json node
     */
    private <A extends Annotation> JsonNode computeNode(
            Parameter[] params,
            Object[] paramsValues,
            Class<A> annotationClass) {

        return this.computeAnnotatedParamValue(params, paramsValues, annotationClass, JsonNode.class)
                .orElse(new ObjectNode(JsonNodeFactory.instance));
    }
}
