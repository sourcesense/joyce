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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.networknt.schema.JsonSchemaException;
import com.samskivert.mustache.Mustache;
import com.sourcesense.joyce.schemaengine.exception.JoyceSchemaEngineException;
import com.sourcesense.joyce.schemaengine.handler.JsonPathTransformerHandler;
import com.sourcesense.joyce.schemaengine.handler.RestTransformerHandler;
import com.sourcesense.joyce.schemaengine.handler.SchemaTransformerHandler;
import com.sourcesense.joyce.schemaengine.handler.ScriptingTransformerHandler;
import com.sourcesense.joyce.schemaengine.model.SchemaEngineContext;
import com.sourcesense.joyce.schemaengine.templating.mustache.resolver.MustacheTemplateResolver;
import com.sourcesense.joyce.schemaengine.test.TestUtility;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class SchemaEngineTest implements TestUtility {

	private RestTemplate restTemplate;
	private MockRestServiceServer mockServer;
	private ApplicationContext applicationContext;
	private MustacheTemplateResolver mustacheTemplateResolver;

	@BeforeEach
	void init() {
		restTemplate = new RestTemplate();
		applicationContext = this.initApplicationContext(jsonMapper);
		mockServer = MockRestServiceServer.createServer(restTemplate);
		mustacheTemplateResolver = new MustacheTemplateResolver(
				jsonMapper,
				Mustache.compiler(),
				this.initMustacheLambdas()
		);
	}

	@Test
	void dummyParserShouldBeRegisteredCorrectly() throws URISyntaxException, IOException {
		String schema = Files.readString(loadResource("schema/10.json"));
		String source = Files.readString(loadResource("source/10.json"));
		SchemaTransformerHandler dummyHandler = mock(SchemaTransformerHandler.class);
		when(dummyHandler.process(any(), any(), any(), any())).thenReturn(new TextNode("foobar"));
		Map<String, SchemaTransformerHandler> handlers = Map.of(
				"extract", dummyHandler
		);
		SchemaEngine<?> schemaEngine = new SchemaEngine<>(jsonMapper, mustacheTemplateResolver, handlers);
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
		Map<String, SchemaTransformerHandler> handlers = Map.of("extract", jsonPathTransformerHandler);
		SchemaEngine<?> schemaEngine = new SchemaEngine<>(jsonMapper, mustacheTemplateResolver,handlers);

		Assertions.assertThrows(
				JsonSchemaException.class,
				() -> schemaEngine.process(schema, source)
		);
	}

	@Test
	void handlersShouldBeAppliedCascading() throws URISyntaxException, IOException {
		String schema = Files.readString(loadResource("schema/20.json"));
		String source = Files.readString(loadResource("source/10.json"));
		JsonNode sourceNode = jsonMapper.readValue(source, JsonNode.class);

		SchemaTransformerHandler handler = mock(SchemaTransformerHandler.class);
		SchemaEngineContext internalContext = SchemaEngineContext.builder()
				.src(sourceNode)
				.out(sourceNode)
				.build();

		when(handler.process(any(), any(), eq(new TextNode("$.email")), eq(internalContext)))
				.thenReturn(new TextNode("mario"));

		when(handler.process(any(), any(), eq(new TextNode("uppercase")), eq(internalContext.withUpdatedOutput(new TextNode("mario")))))
				.thenReturn(new TextNode("MARIO"));

		Map<String, SchemaTransformerHandler> handlers = Map.of(
				"extract", handler,
				"transform", handler
		);

		SchemaEngine<JsonNode> schemaEngine = new SchemaEngine<>(jsonMapper, mustacheTemplateResolver, handlers);
		schemaEngine.defaultInit();

		String expected = "MARIO";
		String actual = schemaEngine.process(schema, source).get("name").asText();

		assertEquals(expected, actual);
	}

	@Test
	void addingFieldShouldNotBreakChanges() throws URISyntaxException, IOException {
		JsonNode schema = this.readNodeFromResource("schema/11.json");
		JsonNode newSchema = this.readNodeFromResource("schema/12.json");
		SchemaEngine<?> schemaEngine = new SchemaEngine<>(jsonMapper, mustacheTemplateResolver, new HashMap<>());
		Boolean ret = schemaEngine.checkForBreakingChanges(schema, newSchema);
		Assertions.assertFalse(ret);
	}

	@Test
	void deprecatingFieldShouldBreakChanges() throws URISyntaxException, IOException {
		JsonNode schema = this.readNodeFromResource("schema/12.json");
		JsonNode newSchema = this.readNodeFromResource("schema/13.json");
		SchemaEngine<?> schemaEngine = new SchemaEngine<>(jsonMapper, mustacheTemplateResolver, new HashMap<>());
		Boolean ret = schemaEngine.checkForBreakingChanges(schema, newSchema);
		assertTrue(ret);
	}

	@Test
	void changingTypeShouldThrow() throws URISyntaxException, IOException {
		JsonNode schema = this.readNodeFromResource("schema/13.json");
		JsonNode newSchema = this.readNodeFromResource("schema/14.json");
		SchemaEngine<?> schemaEngine = new SchemaEngine<>(jsonMapper, mustacheTemplateResolver, new HashMap<>());
		Assertions.assertThrows(JoyceSchemaEngineException.class, () ->
				schemaEngine.checkForBreakingChanges(schema, newSchema)
		);
	}

	@Test
	void changingTypeExtendingTypesShouldNotThrowsAndDoNotBreaks() throws URISyntaxException, IOException {
		JsonNode schema = this.readNodeFromResource("schema/14.json");
		JsonNode newSchema = this.readNodeFromResource("schema/15.json");
		SchemaEngine<?> schemaEngine = new SchemaEngine<>(jsonMapper, mustacheTemplateResolver, new HashMap<>());
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

	@Test
	void shouldProcessWithPythonHandlerOneline() throws URISyntaxException, IOException {
		this.shouldParseSourceWithHandlers("schema/33.yaml", "source/31.json", "result/32.json");
	}

	@Test
	void shouldProcessWithPythonHandlerMultiline() throws URISyntaxException, IOException {
		this.shouldParseSourceWithHandlers("schema/34.yaml", "source/31.json", "result/33.json");
	}

	@Test
	void shouldProcessPostRequestWithRestHandler() throws IOException, URISyntaxException {
		List<String> testHeaders = Arrays.asList("hv1", "hvN");

		mockServer.expect(request -> {
			assertEquals(request.getURI().toString(), "http://test:8080/posts?test=pv1");
			assertEquals(request.getMethod(), HttpMethod.POST);
			assertEquals(request.getBody().toString(), "{\n \"content\": \"test\"\n}\n");
			assertEquals(testHeaders, request.getHeaders().get("test"));

		}).andRespond(withSuccess(
				this.getResourceAsString("rest/response/35.json"),
				MediaType.APPLICATION_JSON
		));

		this.shouldParseSourceWithHandlers("schema/35.yaml", "source/31.json", "result/35.json");
	}

	private void shouldParseSourceWithHandlers(String schemaPath, String sourcePath, String resultPath) throws URISyntaxException, IOException {
		String schemaContent = Files.readString(loadResource(schemaPath));
		String sourceContent = Files.readString(loadResource(sourcePath));
		JsonNode schema = this.computeSchema(schemaPath, schemaContent);
		JsonNode source = jsonMapper.readTree(sourceContent);

		ScriptingTransformerHandler scriptTransformerHandler = new ScriptingTransformerHandler(jsonMapper, applicationContext);

		JsonPathTransformerHandler jsonPathTransformerHandler = new JsonPathTransformerHandler(jsonMapper);
		jsonPathTransformerHandler.configure();

		RestTransformerHandler restTransformerHandler = new RestTransformerHandler(jsonMapper, restTemplate);

		Map<String, SchemaTransformerHandler> handlers = Map.of(
				"script", scriptTransformerHandler,
				"extract", jsonPathTransformerHandler,
				"rest", restTransformerHandler
		);

		SchemaEngine<?> schemaEngine = new SchemaEngine<>(jsonMapper, mustacheTemplateResolver, handlers);
		schemaEngine.defaultInit();

		JsonNode expected = this.getResourceAsNode(resultPath);
		JsonNode actual = schemaEngine.process(schema, source);

		assertEquals(expected, actual);
	}

	private JsonNode computeSchema(
			String schemaPath,
			String schemaContent) throws JsonProcessingException {

		if (schemaPath.endsWith("yaml")) {
			return yamlMapper.readTree(schemaContent);
		} else {
			return jsonMapper.readValue(schemaContent, JsonNode.class);
		}
	}

	private JsonNode readNodeFromResource(String resource) throws URISyntaxException, IOException {
		Path resourcePath = this.loadResource(resource);
		String resourceJson = Files.readString(resourcePath);
		return jsonMapper.readValue(resourceJson, JsonNode.class);
	}
}
