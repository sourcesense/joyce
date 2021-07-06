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
import com.networknt.schema.JsonSchemaException;
import com.sourcesense.joyce.schemaengine.ResourceLoader;
import com.sourcesense.joyce.schemaengine.exception.JoyceSchemaEngineException;
import com.sourcesense.joyce.schemaengine.handler.SchemaTransformerHandler;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class SchemaEngineTest implements ResourceLoader {


	private ObjectMapper mapper;

	@BeforeEach
	void init() {
		mapper = new ObjectMapper();
	}

//  Todo: Invalid Metaschema
//	@Test
//	void dummyParserShouldBeRegisteredCorrectly() throws URISyntaxException, IOException {
//		String schema = Files.readString(loadResource("schema/10.json"));
//		String source = Files.readString(loadResource("source/10.json"));
//		SchemaTransformerHandler dummyHandler = mock(SchemaTransformerHandler.class);
//		when(dummyHandler.process(any(), any(), any(), any(), any())).thenReturn(new TextNode("foobar"));
//		Map<String, SchemaTransformerHandler> handlers = Map.of(
//				"$path", dummyHandler,
//				"$fixed", dummyHandler,
//				"$meta", dummyHandler
//		);
//		SchemaEngine schemaEngine = new SchemaEngine(mapper, handlers);
//		JsonNode result = schemaEngine.process(schema, source);
//		Assertions.assertEquals("Leanne Graham", result.get("name").asText());
//		Assertions.assertEquals("foobar", result.get("mail").asText());
//		Assertions.assertEquals("foobar", result.get("address").asText());
//	}

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

	//Todo: Invalid Metaschema
//	@Test
//	void handlersShouldBeAppliedCascading() throws URISyntaxException, IOException {
//		String schema = Files.readString(loadResource("schema/20.json"));
//		String source = Files.readString(loadResource("source/10.json"));
//
//		SchemaTransformerHandler handler = mock(SchemaTransformerHandler.class);
//		when(handler.process(any(), eq(new TextNode("$.email")), any(), any(), any()))
//				.thenReturn(new TextNode("mario"));
//		when(handler.process(any(), eq(new TextNode("uppercase")), eq(new TextNode("mario")), any(), any()))
//				.thenReturn(new TextNode("MARIO"));
//
//		Map<String, SchemaTransformerHandler> handlers = Map.of(
//				"$path", handler,
//				"$transform", handler
//		);
//
//		SchemaEngine schemaEngine = new SchemaEngine(mapper, handlers);
//
//		String expected = "MARIO";
//		String actual = schemaEngine.process(schema, source).get("name").asText();
//		Assertions.assertEquals(expected, actual);
//	}

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
		Assertions.assertThrows(JoyceSchemaEngineException.class, () -> {
			schemaEngine.checkForBreakingChanges(schema, newSchema);
		});
	}

	@Test
	void changingTypeExtendingTypesShouldNotThrowsAndDoNotBreaks() throws URISyntaxException, IOException {
		JsonNode schema = this.readNodeFromResource("schema/14.json");
		JsonNode newSchema = this.readNodeFromResource("schema/15.json");
		SchemaEngine schemaEngine = new SchemaEngine(mapper, new HashMap<>());
		Assertions.assertFalse(schemaEngine.checkForBreakingChanges(schema, newSchema));
	}

	private JsonNode readNodeFromResource(String resource) throws URISyntaxException, IOException {
		Path resourcePath = this.loadResource(resource);
		String resourceJson = Files.readString(resourcePath);
		return mapper.readValue(resourceJson, JsonNode.class);
	}
}
