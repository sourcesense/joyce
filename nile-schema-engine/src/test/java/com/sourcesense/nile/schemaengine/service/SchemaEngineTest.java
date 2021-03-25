package com.sourcesense.nile.schemaengine.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.sourcesense.nile.schemaengine.dto.ProcessResult;
import com.sourcesense.nile.schemaengine.exceptions.SchemaIsNotValidException;
import com.sourcesense.nile.schemaengine.handlers.JsonPathTransformerHandler;
import com.sourcesense.nile.schemaengine.handlers.TransormerHandler;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
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

	@Test
	void registerHandlerKeyWithoutDollarShouldAddDollarSign() throws URISyntaxException, IOException {
		String schema = Files.readString(loadResource("schema/10.yaml"));
		String source = Files.readString(loadResource("source/10.json"));

		SchemaEngine schemaEngine = new SchemaEngine(props);
		TransormerHandler jsonPathTransformerHandler = Mockito.mock(TransormerHandler.class);
		Mockito.when(jsonPathTransformerHandler.process(any(), any(), any()))
				.thenReturn(new TextNode("foobar"));
		schemaEngine.registerHandler("path", jsonPathTransformerHandler);
		ProcessResult result = schemaEngine.process(schema, source);
		Assertions.assertEquals("Leanne Graham", result.getJson().get("name").asText());
		Assertions.assertEquals("foobar",  result.getJson().get("mail").asText());
		Assertions.assertEquals("foobar", result.getJson().get("address").asText());
	}


	@Test
	void dummyParserShouldBeRegisteredCorrectly() throws URISyntaxException, IOException {
		String schema = Files.readString(loadResource("schema/10.yaml"));
		String source = Files.readString(loadResource("source/10.json"));
		SchemaEngine schemaEngine = new SchemaEngine(props);
		TransormerHandler jsonPathTransformerHandler = Mockito.mock(TransormerHandler.class);
		Mockito.when(jsonPathTransformerHandler.process(any(), any(), any()))
				.thenReturn(new TextNode("foobar"));
		schemaEngine.registerHandler("$path", jsonPathTransformerHandler);
		ProcessResult result = schemaEngine.process(schema, source);
		Assertions.assertEquals("Leanne Graham", result.getJson().get("name").asText());
		Assertions.assertEquals("foobar",  result.getJson().get("mail").asText());
		Assertions.assertEquals("foobar", result.getJson().get("address").asText());
	}

	@Test
	void invalidSchemaShouldThrow() throws URISyntaxException, IOException {
		String schema = Files.readString(loadResource("schema/10.yaml"));
		String source = Files.readString(loadResource("source/10.json"));
		SchemaEngine schemaEngine = new SchemaEngine(props);
		TransormerHandler jsonPathTransformerHandler = Mockito.mock(TransormerHandler.class);

		schemaEngine.registerHandler("$path", jsonPathTransformerHandler);

		SchemaIsNotValidException exc = Assertions.assertThrows(SchemaIsNotValidException.class, () -> {
			schemaEngine.process(schema, source);
		});
	}


	@Test
	void contextShouldReturnTransformed() throws URISyntaxException, IOException {
		String schema = Files.readString(loadResource("schema/10.yaml"));
		String source = Files.readString(loadResource("source/10.json"));
		SchemaEngine schemaEngine = new SchemaEngine(props);
		TransormerHandler jsonPathTransformerHandler = Mockito.mock(TransormerHandler.class);
		Mockito.when(jsonPathTransformerHandler.process(any(), any(), any()))
				.thenReturn(new TextNode("bar"));
		schemaEngine.registerHandler("$path", jsonPathTransformerHandler);

		ProcessResult result = schemaEngine.process(schema, source);
		Assertions.assertEquals("users", result.getContext().get().get("collection"));
		Assertions.assertEquals("bar", result.getContext().get().get("message_key"));
	}
}
