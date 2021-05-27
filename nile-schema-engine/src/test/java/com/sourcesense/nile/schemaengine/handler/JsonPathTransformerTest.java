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

package com.sourcesense.nile.schemaengine.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.TextNode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class JsonPathTransformerTest {
	private ObjectMapper mapper = new ObjectMapper();

	@Test
	void testSimpleJsonPath() throws URISyntaxException, IOException {
		JsonPathTransformerHandler handler = getJsonPathTransformerHandler();
		JsonNode source = getSourceJsonNode();
		JsonNode value = new TextNode("$.email");
		JsonNode result = handler.process("test", value, source, Optional.empty(), Optional.empty());
		Assertions.assertEquals(result.asText(), "Sincere@april.biz");
	}


	@Test
	void testSimpleConcatJsonPath() throws IOException, URISyntaxException {
		JsonPathTransformerHandler handler = getJsonPathTransformerHandler();
		JsonNode source = getSourceJsonNode();
		JsonNode value = new TextNode("$.concat($.address.city, $.address.street)");
		JsonNode result = handler.process("test", value, source, Optional.empty(), Optional.empty());
		Assertions.assertEquals("GwenboroughKulas Light", result.asText());
	}

	@Test
	void testComplexConcatJsonPath() throws IOException, URISyntaxException {
		JsonPathTransformerHandler handler = getJsonPathTransformerHandler();
		JsonNode source = getSourceJsonNode();
		ArrayNode value = mapper.createArrayNode();
		value.add("$.address.city");
		value.add(" - ");
		value.add("$.address.street");
		JsonNode result = handler.process("test",value, source, Optional.empty(), Optional.empty());
		Assertions.assertEquals("Gwenborough - Kulas Light", result.asText());
	}

	private JsonPathTransformerHandler getJsonPathTransformerHandler() {
		JsonPathTransformerHandler handler = new JsonPathTransformerHandler();
		handler.configure();
		return handler;
	}

	private JsonNode getSourceJsonNode() throws IOException, URISyntaxException {
		URL res =  this.getClass().getClassLoader().getResource("source/10.json");
		return mapper.readValue(Files.readString(Path.of((res.toURI()))), JsonNode.class);
	}
}