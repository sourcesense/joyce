package com.sourcesense.nile.core.pipeline;

import com.sourcesense.nile.core.exceptions.PipePanic;

public class Pipeline<I, O> {

    private final Step<I, O> current;

    public Pipeline(Step<I, O> current) {
        this.current = current;
    }

    public <R> Pipeline<I, R> pipe(Step<O, R> next) {
        return new Pipeline<>(input -> next.process(current.process(input)));
    }

    public O execute(I input) throws PipePanic {
        return current.process(input);
    }
}
