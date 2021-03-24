package com.sourcesense.nile.schemaengine.handlers;

import com.fasterxml.jackson.databind.JsonNode;

public interface TransormerHandler {
	JsonNode process(String key, JsonNode schema, JsonNode sourceJsonNode);
}
