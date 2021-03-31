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
public class FixedValueTransformerTest {
	private ObjectMapper mapper = new ObjectMapper();

	@Test
	void testFixedValue() {
		FixedValueTransformerHandler handler = new FixedValueTransformerHandler();
		ObjectNode source = mapper.createObjectNode();
		source.put("foo", "bar");
		JsonNode value = new TextNode("asd");
		JsonNode result = handler.process( value, source, Optional.empty());
		Assertions.assertEquals("asd",result.asText());

		JsonNode result2 = handler.process( source, source, Optional.empty());
		Assertions.assertEquals("bar",result2.get("foo").asText());
	}

}
