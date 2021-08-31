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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class FixedValueTransformerTest {
	private ObjectMapper mapper = new ObjectMapper();

	@Test
	void testFixedValue() {
		FixedValueTransformerHandler handler = new FixedValueTransformerHandler();
		ObjectNode source = mapper.createObjectNode();
		source.put("foo", "bar");
		JsonNode value = new TextNode("asd");
		JsonNode result = handler.process("test", "string", value, source, Optional.empty(), Optional.empty());
		Assertions.assertEquals("asd",result.asText());

		JsonNode result2 = handler.process("test2", "string", source, source, Optional.empty(), Optional.empty());
		Assertions.assertEquals("bar",result2.get("foo").asText());
	}

}
