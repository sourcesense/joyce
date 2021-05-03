package com.sourcesense.nile.core.exceptions;

import lombok.Getter;

@Getter
public class DataInfoNotFoundException extends RuntimeException{

    public DataInfoNotFoundException(String id) {
        super("Raw data info wasn't found for key: '" + id + "'.");
    }
}