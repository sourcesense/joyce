package com.sourcesense.joyce.core.enumeration;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ConnectorOperation {

	CREATE(HttpMethod.POST),
	UPDATE(HttpMethod.PUT),
	DELETE(HttpMethod.DELETE);

	private final HttpMethod method;
}
