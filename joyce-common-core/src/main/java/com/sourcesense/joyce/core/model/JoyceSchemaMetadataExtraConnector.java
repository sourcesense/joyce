package com.sourcesense.joyce.core.model;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JoyceSchemaMetadataExtraConnector {

	private String name;
	private JsonNode config;
}
