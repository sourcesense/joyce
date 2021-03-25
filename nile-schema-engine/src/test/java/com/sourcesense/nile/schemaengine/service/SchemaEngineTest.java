package com.sourcesense.nile.schemaengine.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sourcesense.nile.schemaengine.dto.ProcessResult;
import com.sourcesense.nile.schemaengine.exceptions.SchemaIsNotValidException;
import com.sourcesense.nile.schemaengine.handlers.JsonPathTransformerHandler;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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
	void registerHandlerKeyWithoutDollarShouldAddDollarSign() throws URISyntaxException, IOException {
		String schema = Files.readString(loadResource("schema/10.yaml"));
		String source = Files.readString(loadResource("source/10.json"));
		ObjectNode node = getEmptyObject();
		node.put("mail", "mario");
		node.put("address", "mario");
		Mockito.when(jsonPathTransformerHandler.process(any(), any(), any()))
				.thenReturn(node);

		SchemaEngine schemaEngine = new SchemaEngine(props);
		schemaEngine.registerHandler("path", jsonPathTransformerHandler);
		ProcessResult result = schemaEngine.process(schema, source);
		Assertions.assertTrue(result.getJson().get("name").asText().equals("Leanne Graham"));
		Assertions.assertTrue(result.getJson().get("mail").asText().equals("mario"));
		Assertions.assertTrue(result.getJson().get("address").asText().equals("mario"));
	}

	@Test
	void invalidSchemaShouldThrow() throws URISyntaxException, IOException {
		String schema = Files.readString(loadResource("schema/10.yaml"));
		String source = Files.readString(loadResource("source/10.json"));
		ObjectNode node = getEmptyObject();
		node.put("address", "mario");
		Mockito.when(jsonPathTransformerHandler.process(any(), any(), any()))
				.thenReturn(node);

		SchemaEngine schemaEngine = new SchemaEngine(props);
		schemaEngine.registerHandler("$path", jsonPathTransformerHandler);
		SchemaIsNotValidException exc = Assertions.assertThrows(SchemaIsNotValidException.class, () -> {
			schemaEngine.process(schema, source);
		});
	}

	@Test
	void dummyParserShouldBeRegisteredCorrectly() throws URISyntaxException, IOException {
		String schema = Files.readString(loadResource("schema/10.yaml"));
		String source = Files.readString(loadResource("source/10.json"));
		ObjectNode node = getEmptyObject();
				node.put("mail", "mario");
				node.put("address", "mario");
		Mockito.when(jsonPathTransformerHandler.process(any(), any(), any()))
				.thenReturn(node);

		SchemaEngine schemaEngine = new SchemaEngine(props);
		schemaEngine.registerHandler("$path", jsonPathTransformerHandler);
		ProcessResult result = schemaEngine.process(schema, source);
		Assertions.assertTrue(result.getJson().get("name").asText().equals("Leanne Graham"));
		Assertions.assertTrue(result.getJson().get("mail").asText().equals("mario"));
		Assertions.assertTrue(result.getJson().get("address").asText().equals("mario"));
	}

	@Test
	void contextShouldReturnTransformed() throws URISyntaxException, IOException {
		String schema = Files.readString(loadResource("schema/10.yaml"));
		String source = Files.readString(loadResource("source/10.json"));
		SchemaEngine schemaEngine = new SchemaEngine(props);
		schemaEngine.registerHandler("$path", jsonPathTransformerHandler);
		ObjectNode node = getEmptyObject();
		node.put("mail", "mario");
		node.put("address", "mario");
		Mockito.when(jsonPathTransformerHandler.process(any(), any(), any()))
				.thenReturn(node);

		Mockito.when(jsonPathTransformerHandler.process(ArgumentMatchers.eq("message_key"), any(), any()))
				.thenReturn(getEmptyObject().put("message_key", "bar"));

		ProcessResult result = schemaEngine.process(schema, source);

		Assertions.assertEquals("users", result.getContext().get().get("collection"));
		Assertions.assertEquals("bar", result.getContext().get().get("message_key"));
	}
}
