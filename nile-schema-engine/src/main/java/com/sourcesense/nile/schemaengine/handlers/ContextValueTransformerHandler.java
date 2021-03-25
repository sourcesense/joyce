package com.sourcesense.nile.schemaengine.handlers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sourcesense.nile.schemaengine.dto.SchemaContext;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component("contextValueTransformerHandler")
@Slf4j
@AllArgsConstructor
public class ContextValueTransformerHandler extends JsonPathTransformerHandler {
	private final ObjectMapper mapper;

	@Override
	public JsonNode process(JsonNode value, JsonNode source, Optional<SchemaContext> context) {
		JsonNode contextAsSource = mapper.createObjectNode();
		if (context.isPresent()) {
			contextAsSource = mapper.convertValue(context.get().getContext(), JsonNode.class);
		}
		return super.process(value, contextAsSource, Optional.empty());
	}
}
