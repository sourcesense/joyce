package com.sourcesense.nile.schemaengine.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.sourcesense.nile.schemaengine.dto.ProcessResult;
import com.sourcesense.nile.schemaengine.exceptions.SchemaIsNotValidException;
import com.sourcesense.nile.schemaengine.handlers.TransormerHandler;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

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
		Assertions.assertEquals("Leanne Graham", result.getJson().get("name"));
		Assertions.assertEquals("foobar",  result.getJson().get("mail"));
		Assertions.assertEquals("foobar", result.getJson().get("address"));
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
		Assertions.assertEquals("Leanne Graham", result.getJson().get("name"));
		Assertions.assertEquals("foobar",  result.getJson().get("mail"));
		Assertions.assertEquals("foobar", result.getJson().get("address"));
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
	void metadataShouldReturnTransformed() throws URISyntaxException, IOException {
		String schema = Files.readString(loadResource("schema/10.yaml"));
		String source = Files.readString(loadResource("source/10.json"));
		SchemaEngine schemaEngine = new SchemaEngine(props);
		TransormerHandler jsonPathTransformerHandler = Mockito.mock(TransormerHandler.class);
		Mockito.when(jsonPathTransformerHandler.process(any(), any(), any()))
				.thenReturn(new TextNode("bar"));
		schemaEngine.registerHandler("$path", jsonPathTransformerHandler);

		ProcessResult result = schemaEngine.process(schema, source);
		Assertions.assertEquals("users", result.getMetadata().get().get("collection"));
		Assertions.assertEquals("bar", result.getMetadata().get().get("message_key"));
	}
}
