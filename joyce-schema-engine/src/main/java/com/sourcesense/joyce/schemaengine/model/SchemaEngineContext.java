package com.sourcesense.joyce.schemaengine.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SchemaEngineContext {

	@Builder.Default
	private JsonNode src = JsonNodeFactory.instance.objectNode();

	@Builder.Default
	private JsonNode data = JsonNodeFactory.instance.objectNode();

	@Builder.Default
	private JsonNode metadata = JsonNodeFactory.instance.objectNode();

	@Builder.Default
	private JsonNode out = JsonNodeFactory.instance.objectNode();

	private Object extra;

	public SchemaEngineContext withUpdatedOutput(JsonNode out) {
		return SchemaEngineContext.builder()
				.src(src)
				.data(data)
				.metadata(metadata)
				.out(out)
				.extra(extra)
				.build();
	}
}
