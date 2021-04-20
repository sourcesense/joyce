package com.sourcesense.nile.core.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.sourcesense.nile.core.model.NileSchemaMetadata;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class SchemaSave {

	@JsonProperty("$schema")
	private String schema;

	@JsonProperty("$metadata")
	private NileSchemaMetadata metadata;

	private String type;
	private List<String> required;

	private JsonNode properties;

}
