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
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.PathNotFoundException;
import com.jayway.jsonpath.spi.json.JacksonJsonNodeJsonProvider;
import com.jayway.jsonpath.spi.json.JsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;
import com.jayway.jsonpath.spi.mapper.MappingProvider;
import com.sourcesense.joyce.schemaengine.annotation.SchemaTransformationHandler;
import com.sourcesense.joyce.schemaengine.model.SchemaEngineContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@Slf4j
@Component
@RequiredArgsConstructor
@SchemaTransformationHandler(keyword = "extract")
public class JsonPathTransformerHandler implements SchemaTransformerHandler {

	private final ObjectMapper jsonMapper;

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
	public JsonNode process(String key, String type, JsonNode value, SchemaEngineContext context) {
		if (value.getNodeType().equals(JsonNodeType.ARRAY)){

			StringBuilder stringBuilder = new StringBuilder();
			for (JsonNode jsonNode : value) {
				if(jsonNode.getNodeType().equals(JsonNodeType.STRING)){
					if(jsonNode.asText().startsWith("$")){
						JsonNode resolvedPath = this.computeValueOrDefault(type, context, jsonNode.asText());
						stringBuilder.append(resolvedPath.asText());
					} else {
						stringBuilder.append(jsonNode.asText());
					}
				} else {
					log.error("Values in path handler array must be strings");
				}
			}
			return new TextNode(stringBuilder.toString());
		} else {
			return this.computeValueOrDefault(type, context, value.asText());
		}
	}

	protected JsonNode computeValueOrDefault(String type, SchemaEngineContext context, String pathExpression) {
		List<String> pathExpressionComponents = Arrays.stream(pathExpression.split("\\?\\?"))
				.map(String::trim)
				.collect(Collectors.toList());

		String pathExpr = pathExpressionComponents.get(0);

		JsonNode defaultValue = pathExpressionComponents.size() > 1
				? new TextNode(pathExpressionComponents.get(1))
				: NullNode.getInstance();

		JsonNode source = jsonMapper.convertValue(context, JsonNode.class);
		JsonNode result = this.read(type, source, pathExpr);
		return ! result.isNull() ? result : defaultValue;
	}

	/**
	 * Read the jsonPath from source and handle the case of jsonPath functions
	 * 	 that do not return a JsonNode but a simple string
	 * @param type						Return type
	 * @param source					Source node
	 * @param pathExpression  Expression that dictates how to navigate the source
	 * @return                Retrieved result
	 */
	protected JsonNode read(String type, JsonNode source, String pathExpression){
		try {
			Object resolvedPath = JsonPath.read(source, pathExpression);
			// overcome this limitation of json-path https://github.com/json-path/JsonPath/issues/272
			// if expression is indefinite it always return an array, we force to extract first element when type declared is not an array
			if (resolvedPath instanceof ArrayNode && !type.equals("array")){
				return ((ArrayNode)resolvedPath).get(0);
			} else if(resolvedPath instanceof String){
				return new TextNode((String)resolvedPath);
			}
			return (JsonNode)resolvedPath;
		} catch (PathNotFoundException exc){
			return  NullNode.getInstance();
		}
	}
}
