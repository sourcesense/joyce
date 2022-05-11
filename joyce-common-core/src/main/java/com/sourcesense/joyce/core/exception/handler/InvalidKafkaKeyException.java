package com.sourcesense.joyce.core.exception.handler;

public class InvalidKafkaKeyException extends RuntimeException{

	public InvalidKafkaKeyException(String message) {
		super(message);
	}
}
