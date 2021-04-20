package com.sourcesense.nile.schemaengine.handlers;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Optional;

public interface TransormerHandler {
	JsonNode process(JsonNode value, JsonNode source, Optional<JsonNode> metadata);
}
