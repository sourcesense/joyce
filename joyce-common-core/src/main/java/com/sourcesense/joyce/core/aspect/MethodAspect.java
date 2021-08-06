package com.sourcesense.joyce.core.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Optional;

/**
 * Interface that contains com.sourcesense.joyce.core.utility methods for aspect
 * focused on working on intercepted method signature
 */
public interface MethodAspect {

    /**
     * Retrieves method reflection class from the join point
     *
     * @param joinPoint join point with method signature and args
     * @return method reflection class
     */
    default Method computeMethod(ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        return signature.getMethod();
    }

    /**
     *
     * This method retrieves the value of the first annotated parameter with
     * a chosen annotation and returns an Optional that wraps that value,
     * if the parameter can't be retrieved for any reason
     * it returns an Optional.empty()
     *
     * @param params method reflection parameters
     * @param paramsValues method parameters values
     * @param annotationClass the class of the parameter annotation
     * @param <A> type of the parameter annotation class
     * @return optional that contains the value of the first annotated parameter
     */
    default <A extends Annotation> Optional<Object> computeAnnotatedParamValue(
            Parameter[] params,
            Object[] paramsValues,
            Class<A> annotationClass) {

        try {
            for (int i = 0; i < params.length; i++) {
                if (params[i].isAnnotationPresent(annotationClass)) {
                    return Optional.ofNullable(paramsValues[i]);
                }
            }
            return Optional.empty();

        } catch (Exception exception) {
            return Optional.empty();
        }
    }
}
