package com.sourcesense.joyce.core.model.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.sourcesense.joyce.core.enumeration.ConnectorOperation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConnectorOperationStatus {

	private String name;
	private ConnectorOperation connectorOperation;
	private HttpStatus status;
	private JsonNode body;
}
