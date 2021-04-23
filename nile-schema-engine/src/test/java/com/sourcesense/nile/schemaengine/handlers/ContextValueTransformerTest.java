package com.sourcesense.nile.schemaengine.handlers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class ContextValueTransformerTest {
	private ObjectMapper mapper = new ObjectMapper();

	@Test
	void testFixedValue() {
		MetadataValueTransformerHandler handler = new MetadataValueTransformerHandler(mapper);
		handler.configure();
		ObjectNode source = mapper.createObjectNode();
		source.put("foo", "baz");

		ObjectNode metadataMap = mapper.createObjectNode();
		metadataMap.put("foo", "bar");


		JsonNode value = new TextNode("$.foo");
		JsonNode result = handler.process(null,  value, source, Optional.of(metadataMap));
		Assertions.assertEquals("bar",result.asText());

	}
}
