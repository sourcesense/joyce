package com.sourcesense.nile.core.dto;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

@Data
public class Schema {
	private String uid;
	private Integer version;
	private String name;
	private String description;
	private Boolean development;
	private JsonNode schema;
}
