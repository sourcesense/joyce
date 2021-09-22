package com.sourcesense.joyce.importcore.exception;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.sourcesense.joyce.core.enumeration.ConnectorOperation;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ConnectorOperationException extends RuntimeException{

	private final String connector;
	private final ConnectorOperation connectorOperation;

	public ConnectorOperationException(
			String message,
			String connector,
			ConnectorOperation connectorOperation) {

		super(message);
		this.connector = connector;
		this.connectorOperation = connectorOperation;
	}

	public JsonNode getErrorMessage() {
		return JsonNodeFactory.instance.textNode(this.getMessage());
	}

	public HttpStatus getErrorStatus() {
		return HttpStatus.INTERNAL_SERVER_ERROR;
	}
}

