package com.sourcesense.nile.schemaengine;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
public class SchemaEngine {
	protected static String CONTEXT_KEY = "$context";
	private ObjectMapper yamlReader;
	private ObjectMapper mapper;
	private JsonSchemaFactory factory;

	public SchemaEngine() {
		this.factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7);
		this.yamlReader = new ObjectMapper(new YAMLFactory());
		this.mapper = new ObjectMapper();
	}

	protected Optional<Map<String, Object>> getContext(JsonNode node){
		Optional<JsonNode> contextNode = Optional.ofNullable(node.get(CONTEXT_KEY));
		if(!contextNode.isPresent()){
			return Optional.empty();
		}
		Map<String, Object> context = mapper.convertValue(contextNode.get(), new TypeReference<Map<String, Object>>(){});
		return Optional.of(context);
	}

	/**
	 * Create a new json described by the json-schema provided, with sourceJson as input for the transformation
	 * @param jsonSchema
	 * @param sourceJson
	 * @return
	 */
	public JsonNode process(String jsonSchema, String sourceJson) throws JsonProcessingException {
		JsonNode schemaJsonNode = yamlReader.readValue(jsonSchema, JsonNode.class);
		JsonNode sourceJsonNode = mapper.readValue(sourceJson, JsonNode.class);
		JsonSchema schema = factory.getSchema(schemaJsonNode);
		//TODO: validate schema before parsing
		Optional<Map<String, Object>> context = getContext(schema.getSchemaNode());
		JsonNode result = this.parse(null, schema.getSchemaNode(), sourceJsonNode);
		return result;
	}

	private JsonNode parse(String key, JsonNode schema, JsonNode sourceJsonNode) {

		if (schema.getNodeType().equals(JsonNodeType.OBJECT)){
			sourceJsonNode = this.applyHandlers(key, schema, sourceJsonNode);
			String type = schema.get("type").asText();
			if(type.equals("object")){
				ObjectNode objectNode = mapper.createObjectNode();
				Iterator<Map.Entry<String, JsonNode>> iter = schema.get("properties").fields();
				while (iter.hasNext()) {
					Map.Entry<String, JsonNode> entry = iter.next();
					JsonNode node = this.parse(entry.getKey(), entry.getValue(), sourceJsonNode);
					objectNode.set(entry.getKey(), node);
				}
				return objectNode;
			} else if (type.equals("array")){
				ArrayNode arrayNode = mapper.createArrayNode();
				for (JsonNode item : sourceJsonNode.get(key)){
					JsonNode parsedItem = this.parse(null, schema.get("items"), item);
					arrayNode.add(parsedItem);
				}
				return arrayNode;
			} else {
				return sourceJsonNode.get(key);
			}
		}

		return null;
	}

	private JsonNode applyHandlers(String key, JsonNode schema, JsonNode sourceJsonNode) {

		return null;
	}
}
