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

package com.sourcesense.joyce.schemaengine.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.sourcesense.joyce.schemaengine.model.SchemaEngineContext;
import com.sourcesense.joyce.schemaengine.test.TestUtility;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

@ExtendWith(MockitoExtension.class)
public class JsonPathTransformerHandlerTest implements TestUtility {

	private JsonPathTransformerHandler handler;

	@BeforeEach
	public void init() {
		JsonPathTransformerHandler handler = new JsonPathTransformerHandler(jsonMapper);
		handler.configure();
		this.handler = handler;
	}


	@Test
	void testSimpleJsonPath() throws URISyntaxException, IOException {
		this.testHandlerProcess(
				new TextNode("$.src.email"),
				this.getSourceJsonNode(),
				"Sincere@april.biz"
		);
	}

	@Test
	void testSimpleJsonPathWithValueAndDefaultValue() throws IOException, URISyntaxException {
		this.testHandlerProcess(
				new TextNode("$.src.email ?? default@mail.com"),
				this.getSourceJsonNode(),
				"Sincere@april.biz"
		);
	}

	@Test
	void testSimpleJsonPathWithoutValueButWithDefaultValue() {
		this.testHandlerProcess(
				new TextNode("$.src.email ?? default@mail.com"),
				jsonMapper.createObjectNode(),
				"default@mail.com"
		);
	}

	@Test
	void testSimpleConcatJsonPath() throws IOException, URISyntaxException {
		this.testHandlerProcess(
				new TextNode("$.concat($.src.address.city, $.src.address.street)"),
				this.getSourceJsonNode(),
				"GwenboroughKulas Light"
		);
	}

	@Test
	void testComplexConcatJsonPath() throws IOException, URISyntaxException {
		ArrayNode value = jsonMapper.createArrayNode();
		value.add("$.src.address.city");
		value.add(" - ");
		value.add("$.src.address.street");
		this.testHandlerProcess(value, this.getSourceJsonNode(), "Gwenborough - Kulas Light");
	}

	private void testHandlerProcess(JsonNode value, JsonNode source, String expected) {
		SchemaEngineContext context = SchemaEngineContext.builder()
				.src(source)
				.out(source)
				.build();

		JsonNode result = handler.process("test", "string", value, context);
		Assertions.assertEquals(expected, result.asText());
	}


	private JsonNode getSourceJsonNode() throws IOException, URISyntaxException {
		URL res =  this.getClass().getClassLoader().getResource("source/10.json");
		return jsonMapper.readValue(Files.readString(Path.of((res.toURI()))), JsonNode.class);
	}
}
