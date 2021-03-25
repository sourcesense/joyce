package com.sourcesense.nile.schemaengine.handlers;

import com.fasterxml.jackson.databind.JsonNode;
import com.sourcesense.nile.schemaengine.dto.SchemaContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component("fixedValueTransformerHandler")
@Slf4j
public class FixedValueTransformerHandler implements TransormerHandler{
	@Override
	public JsonNode process(JsonNode value, JsonNode source, Optional<SchemaContext> context) {
		return value;
	}
}
