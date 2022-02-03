package com.sourcesense.joyce.protobuf.exception;

public class ProtobufParsingException extends RuntimeException {

	public ProtobufParsingException(String message) {
		super(message);
	}

	public ProtobufParsingException(Exception exception) {
		super(exception);
	}

	public ProtobufParsingException(String message, Exception exception) {
		super(message, exception);
	}
}
