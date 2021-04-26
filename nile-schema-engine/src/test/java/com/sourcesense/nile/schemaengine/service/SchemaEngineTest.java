package com.sourcesense.nile.schemaengine.service;

import com.fasterxml.jackson.databind.node.TextNode;
import com.sourcesense.nile.schemaengine.dto.ProcessResult;
import com.sourcesense.nile.schemaengine.exceptions.InvalidSchemaException;
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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

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
		String schema = Files.readString(loadResource("schema/10.json"));
		String source = Files.readString(loadResource("source/10.json"));

		SchemaEngine schemaEngine = new SchemaEngine(props);
		TransormerHandler jsonPathTransformerHandler = Mockito.mock(TransormerHandler.class);
		Mockito.when(jsonPathTransformerHandler.process(any(),any(), any(), any(),  any()))
				.thenReturn(new TextNode("foobar"));
		schemaEngine.registerHandler("path", jsonPathTransformerHandler);
		schemaEngine.registerMetaSchema();
		ProcessResult result = schemaEngine.process(schema, source);
		Assertions.assertEquals("Leanne Graham", result.getJson().get("name").asText());
		Assertions.assertEquals("foobar",  result.getJson().get("mail").asText());
		Assertions.assertEquals("foobar", result.getJson().get("address").asText());
	}


	@Test
	void dummyParserShouldBeRegisteredCorrectly() throws URISyntaxException, IOException {
		String schema = Files.readString(loadResource("schema/10.json"));
		String source = Files.readString(loadResource("source/10.json"));
		SchemaEngine schemaEngine = new SchemaEngine(props);
		TransormerHandler jsonPathTransformerHandler = Mockito.mock(TransormerHandler.class);
		Mockito.when(jsonPathTransformerHandler.process(any(),any(), any(), any(),  any()))
				.thenReturn(new TextNode("foobar"));
		schemaEngine.registerHandler("$path", jsonPathTransformerHandler);
		schemaEngine.registerHandler("$fixed", jsonPathTransformerHandler);
		schemaEngine.registerHandler("$meta", jsonPathTransformerHandler);
		schemaEngine.registerMetaSchema();
		ProcessResult result = schemaEngine.process(schema, source);
		Assertions.assertEquals("Leanne Graham", result.getJson().get("name").asText());
		Assertions.assertEquals("foobar",  result.getJson().get("mail").asText());
		Assertions.assertEquals("foobar", result.getJson().get("address").asText());
	}

	@Test
	void invalidSourceShouldThrow() throws URISyntaxException, IOException {
		String schema = Files.readString(loadResource("schema/10.json"));
		String source = Files.readString(loadResource("source/11.json"));
		SchemaEngine schemaEngine = new SchemaEngine(props);
		TransormerHandler jsonPathTransformerHandler = Mockito.mock(TransormerHandler.class);

		schemaEngine.registerHandler("$path", jsonPathTransformerHandler);
		schemaEngine.registerMetaSchema();
		SchemaIsNotValidException exc = Assertions.assertThrows(SchemaIsNotValidException.class, () -> {
			schemaEngine.process(schema, source);
		});
	}

	@Test
	void handlersShouldBeAppliedCascading() throws URISyntaxException, IOException {
		String schema = Files.readString(loadResource("schema/15.json"));
		String source = Files.readString(loadResource("source/10.json"));
		SchemaEngine schemaEngine = new SchemaEngine(props);

		TransormerHandler handler = Mockito.mock(TransormerHandler.class);
		Mockito.when(handler.process(any(),eq(new TextNode("$.email")), any(), any(),  any()))
				.thenReturn(new TextNode("mario"));

		Mockito.when(handler.process(any(),eq(new TextNode("uppercase")), eq(new TextNode("mario")), any(),  any()))
				.thenReturn(new TextNode("MARIO"));

		schemaEngine.registerHandler("$path", handler);
		schemaEngine.registerHandler("$transform", handler);
		schemaEngine.registerMetaSchema();
		ProcessResult result = schemaEngine.process(schema, source);

		Assertions.assertEquals("MARIO", result.getJson().get("name").asText());

	}

	@Test
	void metadataShouldReturnTransformed() throws URISyntaxException, IOException {
		String schema = Files.readString(loadResource("schema/10.json"));
		String source = Files.readString(loadResource("source/10.json"));
		SchemaEngine schemaEngine = new SchemaEngine(props);
		TransormerHandler jsonPathTransformerHandler = Mockito.mock(TransormerHandler.class);
		Mockito.when(jsonPathTransformerHandler.process(any(),any(), any(), any(),  any()))
				.thenReturn(new TextNode("bar"));
		schemaEngine.registerHandler("$path", jsonPathTransformerHandler);
		schemaEngine.registerMetaSchema();

		ProcessResult result = schemaEngine.process(schema, source);
		Assertions.assertEquals("users", result.getMetadata().get().get("collection").asText());
	}

    @Test
    void schemaWithVersionBreakingChangeShouldReturnTrue() throws URISyntaxException, IOException {
		String schema = Files.readString(loadResource("schema/11.json"));
		String newSchema = Files.readString(loadResource("schema/12.json"));;
		SchemaEngine schemaEngine = new SchemaEngine(props);
		Boolean ret = schemaEngine.hasBreakingChanges(schema, newSchema);
		Assertions.assertTrue(ret);
    }

	@Test
	void schemaWithChangingSourceOnAFieldShouldReturnFalse() throws URISyntaxException, IOException {
		String schema = Files.readString(loadResource("schema/11.json"));
		String newSchema = Files.readString(loadResource("schema/14.json"));;
		SchemaEngine schemaEngine = new SchemaEngine(props);
		Boolean ret = schemaEngine.hasBreakingChanges(schema, newSchema);
		Assertions.assertFalse(ret);
	}

	@Test
	void changingTypeWithoutRenamingShouldThrow() throws URISyntaxException, IOException {
		String schema = Files.readString(loadResource("schema/12.json"));
		String newSchema = Files.readString(loadResource("schema/13.json"));;
		SchemaEngine schemaEngine = new SchemaEngine(props);
		Assertions.assertThrows(InvalidSchemaException.class, () -> {
			schemaEngine.hasBreakingChanges(schema, newSchema);
		});
	}

	@Test
	void changingTypeExtendingTypesWithArrayShouldNotThroes() throws URISyntaxException, IOException {
		String schema = Files.readString(loadResource("schema/12.json"));
		String newSchema = Files.readString(loadResource("schema/17.json"));;
		SchemaEngine schemaEngine = new SchemaEngine(props);
		Assertions.assertTrue(schemaEngine.hasBreakingChanges(schema, newSchema));
	}


}
