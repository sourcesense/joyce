package com.sourcesense.nile.schemaengine.handlers;

import com.fasterxml.jackson.databind.JsonNode;
import com.sourcesense.nile.schemaengine.dto.SchemaContext;

import java.util.Optional;

public interface TransormerHandler {
	JsonNode process(JsonNode value, JsonNode source, Optional<SchemaContext> context);
}
