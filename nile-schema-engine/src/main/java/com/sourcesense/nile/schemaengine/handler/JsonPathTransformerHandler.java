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
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.TextNode;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.spi.json.JacksonJsonNodeJsonProvider;
import com.jayway.jsonpath.spi.json.JsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;
import com.jayway.jsonpath.spi.mapper.MappingProvider;
import com.sourcesense.nile.schemaengine.annotation.SchemaTransformationHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;


@Slf4j
@Component
@SchemaTransformationHandler(keyword = "$path")
public class JsonPathTransformerHandler implements SchemaTransformerHandler {

	@PostConstruct
	public void configure(){
		Configuration.setDefaults(new Configuration.Defaults() {

			private final JsonProvider jsonProvider = new JacksonJsonNodeJsonProvider();
			private final MappingProvider mappingProvider = new JacksonMappingProvider();

			@Override
			public JsonProvider jsonProvider() {
				return jsonProvider;
			}

			@Override
			public MappingProvider mappingProvider() {
				return mappingProvider;
			}

			@Override
			public Set<Option> options() {
				return EnumSet.noneOf(Option.class);
			}
		});
	}

	@Override
	public JsonNode process(String key, JsonNode value, JsonNode source, Optional<JsonNode> metadata, Optional<Object> context) {
		if (value.getNodeType().equals(JsonNodeType.ARRAY)){
			StringBuffer stringBuffer = new StringBuffer();
			for (JsonNode jsonNode : value) {
				if(jsonNode.getNodeType().equals(JsonNodeType.STRING)){
					if(jsonNode.asText().startsWith("$")){
						JsonNode resolvedPath = this.read(source, jsonNode.asText());
						stringBuffer.append(resolvedPath.asText());
					} else {
						stringBuffer.append(jsonNode.asText());
					}
				} else {
					log.error("values in array must be strings ");
				}
			}
			return new TextNode(stringBuffer.toString());
		} else {
			return this.read(source, value.asText());
		}
	}

	/**
	 * Read the jsonPath from source and handle the case of jsonPath functions
	 * 	 that do not return a JsonNode but a simple string
	 * @param source
	 * @param pathExpression
	 * @return
	 */
	private JsonNode read(JsonNode source, String pathExpression){
		Object resolvedPath = JsonPath.read(source, pathExpression);
		if (resolvedPath.getClass().equals(String.class)){
			return new TextNode((String) resolvedPath);
		} else {
			return (JsonNode)resolvedPath;
		}
	}
}
