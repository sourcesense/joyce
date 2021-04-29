package com.sourcesense.nile.core.exceptions;

/**
 * DON'T PANIC
 * <p>
 * questa eccezione ha lo scopo di bloccare una pipeline in corso
 */


public class PipePanic extends Exception {

    public PipePanic(String msg) {
        super(msg);
    }
}
