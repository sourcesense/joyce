package com.sourcesense.nile.core.errors;

@FunctionalInterface
public interface FunctionThrowingException<T, R> {
    R apply(T t) throws Exception;
}
