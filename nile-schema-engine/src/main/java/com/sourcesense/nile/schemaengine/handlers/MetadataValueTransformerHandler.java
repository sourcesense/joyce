package com.sourcesense.nile.schemaengine.handlers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component("metadataValueTransformerHandler")
@Slf4j
@AllArgsConstructor
public class MetadataValueTransformerHandler extends JsonPathTransformerHandler {
	private final ObjectMapper mapper;

	@Override
	public JsonNode process(JsonNode value, JsonNode source, Optional<JsonNode> metadata) {
		JsonNode metadataAsSource = mapper.createObjectNode();
		if (metadata.isPresent()) {
			metadataAsSource = metadata.get();
		}
		return super.process(value, metadataAsSource, Optional.empty());
	}
}
