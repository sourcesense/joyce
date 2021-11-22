package com.sourcesense.joyce.core.exception;

public class TestRuntimeException extends RuntimeException{

	public TestRuntimeException(String message, Exception exception) {
		super(message, exception);
	}
}
