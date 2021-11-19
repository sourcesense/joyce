package com.sourcesense.joyce.sink.mongodb.exception;

public class MongodbSinkException extends RuntimeException {
    public MongodbSinkException(String exception) {
        super(exception);
    }
}
