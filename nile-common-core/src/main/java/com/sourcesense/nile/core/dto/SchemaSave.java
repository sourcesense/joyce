package com.sourcesense.nile.core.dto;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

import java.util.Map;

@Data
public class SchemaSave {
	private String name;
	private JsonNode schema;
	private String description;
	private Boolean development;
}
