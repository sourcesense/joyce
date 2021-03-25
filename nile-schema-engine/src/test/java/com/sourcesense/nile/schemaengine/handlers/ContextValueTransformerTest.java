package com.sourcesense.nile.schemaengine.handlers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.sourcesense.nile.schemaengine.dto.SchemaContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class ContextValueTransformerTest {
	private ObjectMapper mapper = new ObjectMapper();

	@Test
	void testFixedValue() {
		ContextValueTransformerHandler handler = new ContextValueTransformerHandler(mapper);
		handler.configure();
		ObjectNode source = mapper.createObjectNode();
		source.put("foo", "baz");

		Map<String, Object> contextMap = new HashMap<>();
		contextMap.put("foo", "bar");
		SchemaContext schemaContext = new SchemaContext(contextMap);

		JsonNode value = new TextNode("$.foo");
		JsonNode result = handler.process( value, source, Optional.of(schemaContext));
		Assertions.assertEquals("bar",result.asText());

	}
}
