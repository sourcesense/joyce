package com.sourcesense.nile.schemaengine.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sourcesense.nile.schemaengine.exceptions.HandlerKeyMustStartWithDollarException;
import com.sourcesense.nile.schemaengine.exceptions.SchemaIsNotValidException;
import com.sourcesense.nile.schemaengine.handlers.JsonPathTransformerHandler;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
public class SchemaEngineTest {

	protected Path loadResource(String name) throws URISyntaxException {
		URL res =  this.getClass().getClassLoader().getResource(name);
		return Paths.get(res.toURI());
	}

	protected ObjectNode getEmptyObject(){
		ObjectMapper mapper = new ObjectMapper();
		return mapper.createObjectNode();
	}

	@Mock
	SchemaEngineProperties props;

	@Mock
	JsonPathTransformerHandler jsonPathTransformerHandler;

	@Test
	void registerHandlerWithWrogKeyShouldThrow(){
		SchemaEngine schemaEngine = new SchemaEngine(props);
		assertThrows(HandlerKeyMustStartWithDollarException.class, () -> {
			schemaEngine.registerHandler("path", jsonPathTransformerHandler);
		});
	}

	@Test
	void invalidSchemaShouldThrow() throws URISyntaxException, IOException {
		String schema = Files.readString(loadResource("schema-01.yaml"));
		String source = Files.readString(loadResource("source-01.json"));
		ObjectNode node = getEmptyObject();
		node.put("address", "mario");
		Mockito.when(jsonPathTransformerHandler.process(any(), any(), any()))
				.thenReturn(node);

		SchemaEngine schemaEngine = new SchemaEngine(props);
		schemaEngine.registerHandler("$path", jsonPathTransformerHandler);
		SchemaIsNotValidException exc = assertThrows(SchemaIsNotValidException.class, () -> {
			schemaEngine.process(schema, source);
		});
	}

	@Test
	void dummyParserShouldBeRegisteredCorrectly() throws URISyntaxException, IOException {
		String schema = Files.readString(loadResource("schema-01.yaml"));
		String source = Files.readString(loadResource("source-01.json"));
		ObjectNode node = getEmptyObject();
				node.put("mail", "mario");
				node.put("address", "mario");
		Mockito.when(jsonPathTransformerHandler.process(any(), any(), any()))
				.thenReturn(node);

		SchemaEngine schemaEngine = new SchemaEngine(props);
		schemaEngine.registerHandler("$path", jsonPathTransformerHandler);
		JsonNode result = schemaEngine.process(schema, source);
		assertTrue(result.get("name").asText().equals("Leanne Graham"));
		assertTrue(result.get("mail").asText().equals("mario"));
		assertTrue(result.get("address").asText().equals("mario"));
	}
}
