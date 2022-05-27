package com.sourcesense.joyce.schemaengine.model.dto.schema;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SchemaPropertyHandler {

	private String handler;
	private JsonNode args;
}
