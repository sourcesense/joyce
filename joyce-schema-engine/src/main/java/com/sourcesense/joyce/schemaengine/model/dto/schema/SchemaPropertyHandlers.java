package com.sourcesense.joyce.schemaengine.model.dto.schema;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SchemaPropertyHandlers {

	private JsonNode type;
	private String value;

	@Builder.Default
	private List<SchemaPropertyHandler> apply = Collections.emptyList();
}
