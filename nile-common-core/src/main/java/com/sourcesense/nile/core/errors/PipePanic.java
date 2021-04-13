package com.sourcesense.nile.core.errors;

import java.util.List;

/**
 * DON'T PANIC
 * <p>
 * questa eccezione ha lo scopo di bloccare una pipeline in corso
 */


public class PipePanic extends Exception {

    //Todo: Definire lista di eventi

    private final transient List<?> outputEvents;

    public PipePanic(String msg, List<?> outputEvents) {
        super(msg);

        this.outputEvents = outputEvents;
    }

    public List<?> getOutputEvents() {
        return outputEvents;
    }
}
