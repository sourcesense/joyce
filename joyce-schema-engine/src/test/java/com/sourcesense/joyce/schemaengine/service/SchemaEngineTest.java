/*
 *  Copyright 2021 Sourcesense Spa
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sourcesense.joyce.schemaengine.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.TextNode;
import com.networknt.schema.JsonSchemaException;
import com.sourcesense.joyce.schemaengine.exception.JoyceSchemaEngineException;
import com.sourcesense.joyce.schemaengine.handler.*;
import com.sourcesense.joyce.schemaengine.utility.UtilitySupplier;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.context.ApplicationContext;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class SchemaEngineTest implements UtilitySupplier {


	private ObjectMapper mapper;
	private ApplicationContext applicationContext;

	@BeforeEach
	void init() {
		mapper = this.initMapper();
		applicationContext = this.initApplicationContext(mapper);
	}

	@Test
	void dummyParserShouldBeRegisteredCorrectly() throws URISyntaxException, IOException {
		String schema = Files.readString(loadResource("schema/10.json"));
		String source = Files.readString(loadResource("source/10.json"));
		SchemaTransformerHandler dummyHandler = mock(SchemaTransformerHandler.class);
		when(dummyHandler.process(any(), any(), any(), any(), any())).thenReturn(new TextNode("foobar"));
		Map<String, SchemaTransformerHandler> handlers = Map.of(
				"$path", dummyHandler,
				"$fixed", dummyHandler,
				"$meta", dummyHandler
		);
		SchemaEngine schemaEngine = new SchemaEngine(mapper, handlers);
		schemaEngine.defaultInit();

		JsonNode result = schemaEngine.process(schema, source);
		assertEquals("Leanne Graham", result.get("name").asText());
		assertEquals("foobar", result.get("mail").asText());
		assertEquals("foobar", result.get("address").asText());
	}

	@Test
	void invalidSourceShouldThrow() throws URISyntaxException, IOException {
		String schema = Files.readString(loadResource("schema/10.json"));
		String source = Files.readString(loadResource("source/11.json"));
		SchemaTransformerHandler jsonPathTransformerHandler = mock(SchemaTransformerHandler.class);
		Map<String, SchemaTransformerHandler> handlers = Map.of("$path", jsonPathTransformerHandler);
		SchemaEngine schemaEngine = new SchemaEngine(mapper, handlers);

		Assertions.assertThrows(
				JsonSchemaException.class,
				() -> schemaEngine.process(schema, source)
		);
	}

	@Test
	void handlersShouldBeAppliedCascading() throws URISyntaxException, IOException {
		String schema = Files.readString(loadResource("schema/20.json"));
		String source = Files.readString(loadResource("source/10.json"));

		SchemaTransformerHandler handler = mock(SchemaTransformerHandler.class);
		when(handler.process(any(), eq(new TextNode("$.email")), any(), any(), any()))
				.thenReturn(new TextNode("mario"));
		when(handler.process(any(), eq(new TextNode("uppercase")), eq(new TextNode("mario")), any(), any()))
				.thenReturn(new TextNode("MARIO"));

		Map<String, SchemaTransformerHandler> handlers = Map.of(
				"$path", handler,
				"$transform", handler
		);

		SchemaEngine schemaEngine = new SchemaEngine(mapper, handlers);
		schemaEngine.defaultInit();

		String expected = "MARIO";
		String actual = schemaEngine.process(schema, source).get("name").asText();

		assertEquals(expected, actual);
	}

	@Test
	void addingFieldShouldNotBreakChanges() throws URISyntaxException, IOException {
		JsonNode schema = this.readNodeFromResource("schema/11.json");
		JsonNode newSchema = this.readNodeFromResource("schema/12.json");
		SchemaEngine schemaEngine = new SchemaEngine(mapper, new HashMap<>());
		Boolean ret = schemaEngine.checkForBreakingChanges(schema, newSchema);
		Assertions.assertFalse(ret);
	}

	@Test
	void deprecatingFieldShouldBreakChanges() throws URISyntaxException, IOException {
		JsonNode schema = this.readNodeFromResource("schema/12.json");
		JsonNode newSchema = this.readNodeFromResource("schema/13.json");
		SchemaEngine schemaEngine = new SchemaEngine(mapper, new HashMap<>());
		Boolean ret = schemaEngine.checkForBreakingChanges(schema, newSchema);
		Assertions.assertTrue(ret);
	}

	@Test
	void changingTypeShouldThrow() throws URISyntaxException, IOException {
		JsonNode schema = this.readNodeFromResource("schema/13.json");
		JsonNode newSchema = this.readNodeFromResource("schema/14.json");
		SchemaEngine schemaEngine = new SchemaEngine(mapper, new HashMap<>());
		Assertions.assertThrows(JoyceSchemaEngineException.class, () ->
			schemaEngine.checkForBreakingChanges(schema, newSchema)
		);
	}

	@Test
	void changingTypeExtendingTypesShouldNotThrowsAndDoNotBreaks() throws URISyntaxException, IOException {
		JsonNode schema = this.readNodeFromResource("schema/14.json");
		JsonNode newSchema = this.readNodeFromResource("schema/15.json");
		SchemaEngine schemaEngine = new SchemaEngine(mapper, new HashMap<>());
		Assertions.assertFalse(schemaEngine.checkForBreakingChanges(schema, newSchema));
	}

	@Test
	void shouldParseArrayWithJavascriptHandler() throws URISyntaxException, IOException {
		this.shouldParseSourceWithHandlers("schema/31.json", "source/31.json", "result/31.json");
	}

	@Test
	void shouldParseObjectWithJavascriptHandler() throws URISyntaxException, IOException {
		this.shouldParseSourceWithHandlers("schema/32.json", "source/31.json", "result/32.json");
	}

	private void shouldParseSourceWithHandlers(String schemaPath, String sourcePath, String resultPath) throws URISyntaxException, IOException {
		String schema = Files.readString(loadResource(schemaPath));
		String source = Files.readString(loadResource(sourcePath));

		ScriptingTransformerHandler scriptTransformerHandler = new ScriptingTransformerHandler(mapper, applicationContext);
		FixedValueTransformerHandler fixedValueTransformerHandler = new FixedValueTransformerHandler();
		MetadataValueTransformerHandler metadataValueTransformerHandler = new MetadataValueTransformerHandler(mapper);
		JsonPathTransformerHandler jsonPathTransformerHandler = new JsonPathTransformerHandler();
		jsonPathTransformerHandler.configure();

		Map<String, SchemaTransformerHandler> handlers = Map.of(
				"$script", scriptTransformerHandler,
				"$path", jsonPathTransformerHandler,
				"$meta", metadataValueTransformerHandler,
				"$fixed", fixedValueTransformerHandler
				);

		SchemaEngine schemaEngine = new SchemaEngine(mapper, handlers);
		schemaEngine.defaultInit();

		JsonNode expected = this.getResourceAsNode(resultPath);
		JsonNode actual = schemaEngine.process(schema, source);

		assertEquals(expected, actual);
	}

	private JsonNode readNodeFromResource(String resource) throws URISyntaxException, IOException {
		Path resourcePath = this.loadResource(resource);
		String resourceJson = Files.readString(resourcePath);
		return mapper.readValue(resourceJson, JsonNode.class);
	}
}
