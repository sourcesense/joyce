package com.sourcesense.nile.core.pipeline;


import com.sourcesense.nile.core.errors.PipePanic;

public interface Step<I, O> {

    public O process(I input) throws PipePanic;

}
