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

package com.sourcesense.joyce.core.mapping.jackson.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.sourcesense.joyce.core.exception.handler.JsonProcessingException;
import com.sourcesense.joyce.core.model.uri.JoyceURI;
import com.sourcesense.joyce.core.model.uri.JoyceURIFactory;
import lombok.Getter;

import java.io.IOException;
import java.util.Optional;

@Getter
public class JoyceURIDeserializer<J extends JoyceURI> extends StdDeserializer<J> {

	private final JoyceURIFactory joyceURIFactory;
	private final Class<J> joyceURIClass;

	public JoyceURIDeserializer(JoyceURIFactory joyceURIFactory, Class<J> joyceURIClass) {
		super(joyceURIClass);
		this.joyceURIClass = joyceURIClass;
		this.joyceURIFactory = joyceURIFactory;
	}

	@Override
	public J deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
		JsonNode uriNode = jsonParser.getCodec().readTree(jsonParser);
		return Optional.ofNullable(uriNode)
				.map(node -> node.isObject() ? node.get("uri").asText() : node.asText())
				.flatMap(stringURI -> joyceURIFactory.createURI(stringURI, joyceURIClass))
				.orElseThrow(() -> new JsonProcessingException(String.format(
						"uri: %s is not a joyce uri", uriNode.asText())
				));
	}
}
