package com.sourcesense.nile.core.pipeline;


import com.sourcesense.nile.core.errors.PipePanic;

@FunctionalInterface
public interface Step<I, O> {

    O process(I input) throws PipePanic;

}
