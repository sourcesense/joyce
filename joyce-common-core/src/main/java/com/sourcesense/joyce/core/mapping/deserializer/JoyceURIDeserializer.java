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

package com.sourcesense.joyce.core.mapping.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.sourcesense.joyce.core.model.uri.JoyceURI;
import com.sourcesense.joyce.core.model.uri.JoyceURIFactory;

import java.io.IOException;
import java.util.Optional;

public class JoyceURIDeserializer extends StdDeserializer<JoyceURI> {
	protected JoyceURIDeserializer() {
		this(null);
	}

	protected JoyceURIDeserializer(Class<?> vc) {
		super(vc);
	}

	@Override
	public JoyceURI deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
		JsonNode uriNode = jsonParser.getCodec().readTree(jsonParser);
		return Optional.ofNullable(uriNode)
				.map(node -> node.isObject() ? node.get("uri").asText() : node.asText())
				.flatMap(JoyceURIFactory.getInstance()::createURI)
				.orElseThrow(() -> new JsonProcessingException(String.format(
						"uri: %s is not a joyce uri", uriNode.asText())
				) {});
	}
}
