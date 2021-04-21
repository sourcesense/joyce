package com.sourcesense.nile.schemaengine.handlers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.TextNode;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.spi.json.JacksonJsonNodeJsonProvider;
import com.jayway.jsonpath.spi.json.JsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;
import com.jayway.jsonpath.spi.mapper.MappingProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;

@Component("jsonPathTransformerHandler")
@Slf4j
public class JsonPathTransformerHandler implements TransormerHandler {

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
	public JsonNode process(JsonNode value, JsonNode source, Optional<JsonNode> metadata) {
		if (value.getNodeType().equals(JsonNodeType.ARRAY)){
			StringBuffer stringBuffer = new StringBuffer();
			for (JsonNode jsonNode : ((ArrayNode) value)) {
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