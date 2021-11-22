package com.sourcesense.joyce.core.exception;

import org.springframework.http.ResponseEntity;

public class RestException extends RuntimeException {

	public RestException(String message) {
		super(message);
	}

	public <T> RestException(String endpoint, ResponseEntity<T> response) {
		super(
				String.format(
						"An error happened while calling '%s'. Response has an error status code. StatusCode: '%s'. ResponseBody: %s",
						endpoint, response.getStatusCode(), response.getBody()
				)
		);
	}
}
