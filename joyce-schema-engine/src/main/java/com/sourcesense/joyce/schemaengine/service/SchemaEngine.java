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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.*;
import com.networknt.schema.*;
import com.sourcesense.joyce.schemaengine.JoyceMetaSchema;
import com.sourcesense.joyce.schemaengine.exception.InvalidSchemaException;
import com.sourcesense.joyce.schemaengine.exception.JoyceSchemaEngineException;
import com.sourcesense.joyce.schemaengine.handler.SchemaTransformerHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@Slf4j
@DependsOn("transformerHandlers")
public class SchemaEngine{

	public static final String METADATA = "$metadata";

	private final ObjectMapper mapper;
	private final Map<String, SchemaTransformerHandler> transformerHandlers;

	private JsonSchemaFactory factory;

	public SchemaEngine(
			ObjectMapper mapper,
			@Qualifier("transformerHandlers") Map<String, SchemaTransformerHandler> transformerHandlers) {

		this.mapper = mapper;
		this.transformerHandlers = transformerHandlers;
		this.factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7);
	}

	/**
	 * Default initialization of the Engine when initialized by Spring
	 */
	@PostConstruct
	public void defaultInit() {
		this.registerMetaSchema();
		this.logRegisteredTransformerHandlers();
	}

	/**
	 * Generates the Meta Schema with correct registered Keywords
	 */
	private void registerMetaSchema() {
		JsonMetaSchema metaSchema = JoyceMetaSchema.getInstance(transformerHandlers.keySet());
		this.factory = new JsonSchemaFactory.Builder()
				.defaultMetaSchemaURI(metaSchema.getUri())
				.addMetaSchema(metaSchema)
				.build();
	}

	private void logRegisteredTransformerHandlers() {
		log.info("The following {} schema transformer handlers have been loaded: {}",
				transformerHandlers.size(), transformerHandlers.keySet());
	}

	/**
	 * Main Parse method, applies the transformer and navigate the schema calling parse recursively
	 * Root key could be null
	 * the schema is the json-schema definition
	 * source is the json source node from wich the transformation are applied
	 * @param key
	 * @param schema
	 * @param sourceJsonNode
	 * @param context
	 * @return
	 */
	private JsonNode parse(String key, JsonNode schema, JsonNode sourceJsonNode, Optional<JsonNode> metadata, Optional<Object> context) {
		try {
			if (schema.getNodeType().equals(JsonNodeType.OBJECT)){
				// Apply custom handlers
				Optional<JsonNode> transformed = this.applyHandlers(key, schema, sourceJsonNode, metadata, context);
				if(transformed.isPresent()){
					ObjectNode node = mapper.createObjectNode();
					node.set(key, transformed.get());
					ObjectNode tempSchema = schema.deepCopy();
					tempSchema.remove(transformerHandlers.keySet());

					tempSchema.set("type", schema.get("type"));
					return this.parse(key, tempSchema, node, metadata, context);
				}
				// TODO: parse and handle "$ref"
				JsonNode type = Optional.ofNullable(schema.get("type")).orElse(new TextNode("string"));
				if (type.getNodeType().equals(JsonNodeType.ARRAY)){

					for (JsonNode aType : type){
						JsonNode result = parseType(key, schema, sourceJsonNode, metadata, context, aType.asText());
						if (result != null){
							return result;
						}
					}
					return null;
				} else {
					return parseType(key, schema, sourceJsonNode, metadata, context, type.asText());
				}


			}
		} catch (Exception e){
			throw new JoyceSchemaEngineException(String.format("Cannot parse [%s]: %s", key, e.getMessage()));
		}

		return null;
	}

	private JsonNode parseType(String key, JsonNode schema, JsonNode sourceJsonNode, Optional<JsonNode> metadata, Optional<Object> context, String type) {
		if(type.equals("object")){
			ObjectNode objectNode = mapper.createObjectNode();
			JsonNode props = schema.get("properties");
			if (props != null) {
				Iterator<Map.Entry<String, JsonNode>> iter = props.fields();
				while (iter.hasNext()) {
					Map.Entry<String, JsonNode> entry = iter.next();
					JsonNode node = this.parse(entry.getKey(), entry.getValue(), sourceJsonNode, metadata, context);
					objectNode.set(entry.getKey(), node);
				}
			}
			return objectNode;
		} else if (type.equals("array")){
			ArrayNode arrayNode = mapper.createArrayNode();
			for (JsonNode item : sourceJsonNode.get(key)){
				JsonNode parsedItem = this.parse(null, schema.get("items"), item, metadata, context);
				arrayNode.add(parsedItem);
			}
			return arrayNode;
		} else if (type.equals("integer")){
			return sourceJsonNode.get(key).asText().isEmpty() ? null :  JsonNodeFactory.instance.numberNode(Integer.parseInt(sourceJsonNode.get(key).asText()));
		} else if (type.equals("number")){
			return sourceJsonNode.get(key).asText().isEmpty() ? null :JsonNodeFactory.instance.numberNode(Double.parseDouble(sourceJsonNode.get(key).asText()));
		} else if (type.equals("null")){
			return null;
		} else {
			//TODO: handle other types and modification ie range and other limitations
			return sourceJsonNode.get(key);
		}
	}

	/**
	 * Apply registered handlers in cascade sing output from the first as input to the following
	 *
	 * @param key
	 * @param schema
	 * @param sourceJsonNode
	 * @param context
	 * @return
	 */
	private Optional<JsonNode> applyHandlers(String key, JsonNode schema, JsonNode sourceJsonNode, Optional<JsonNode> metadata, Optional<Object> context) {

		if(sourceJsonNode.getNodeType() != JsonNodeType.OBJECT){
			return Optional.empty();
		}

		// Apply handlers in cascade
		List<String> knownHandlerKeys = StreamSupport.stream(
				Spliterators.spliteratorUnknownSize(schema.fieldNames(), Spliterator.ORDERED),
				false)
				.filter(transformerHandlers.keySet()::contains)
				.collect(Collectors.toList());
		if (knownHandlerKeys.size() < 1){
			return Optional.empty();
		}

		JsonNode returnNode = sourceJsonNode.deepCopy();
		for (String handlerKey : knownHandlerKeys){
				SchemaTransformerHandler handler = transformerHandlers.get(handlerKey);
				returnNode = handler.process(key, schema.get(handlerKey), returnNode, metadata, context);

		}
		return Optional.ofNullable(returnNode);
	}


	/**
	 * Create a new json described by the json-schema provided, with sourceJson as input for the transformation
	 * with schema and source provided as string
	 *
	 * @param jsonSchema
	 * @param sourceJson
	 * @return
	 */
	public JsonNode process(String jsonSchema, String sourceJson) throws JsonProcessingException {
		JsonNode schemaJsonNode = mapper.readValue(jsonSchema, JsonNode.class);
		JsonNode sourceJsonNode = mapper.readValue(sourceJson, JsonNode.class);
		return process(schemaJsonNode, sourceJsonNode, null);
	}

	/**
	 * Utility method
	 *
	 * @param jsonSchema
	 * @param sourceJson
	 * @return
	 * @throws JsonProcessingException
	 */
	public JsonNode process(String jsonSchema, Map sourceJson) throws JsonProcessingException {
		JsonNode schemaJsonNode = mapper.readValue(jsonSchema, JsonNode.class);
		JsonNode sourceJsonNode = mapper.convertValue(sourceJson, JsonNode.class);
		return process(schemaJsonNode, sourceJsonNode, null);
	}

	public JsonNode process(Map jsonSchema, Map sourceJson) {
		JsonNode schemaJsonNode = mapper.convertValue(jsonSchema, JsonNode.class);
		JsonNode sourceJsonNode = mapper.convertValue(sourceJson, JsonNode.class);
		return process(schemaJsonNode, sourceJsonNode, null);
	}

	/**
	 * Create a new json described by the json-schema provided, with sourceJson as input for the transformation
	 *
	 * @param schema
	 * @param source
	 * @return
	 */
	public JsonNode process(JsonNode schema, JsonNode source, Object context) {
		JsonSchema jsonSchema = factory.getSchema(schema);
		Optional<JsonNode> metadata = Optional.ofNullable(jsonSchema.getSchemaNode().get(METADATA));
		JsonNode result = this.parse(null, jsonSchema.getSchemaNode(), source, metadata, Optional.ofNullable(context));

		validate(schema, result);

		return result;
	}

	/**
	 * Validate a document against a json schema
	 * throws if it is not valid
	 * @param schema
	 * @param content
	 */
	public void validate(JsonNode schema, JsonNode content) {
		JsonSchema jsonSchema = factory.getSchema(schema);
		ValidationResult validation = jsonSchema.validateAndCollect(content);
		if(validation.getValidationMessages().size() > 0){
			throw new InvalidSchemaException(validation);
		}
	}

	/**
	 * Detects if the changes in the schema are breaking changes or not, throws Exception if the schema changes are unacceptable
	 * @param previousSchema
	 * @param newSchema
	 * @return
	 */
	public Boolean checkForBreakingChanges(String previousSchema, String newSchema) throws JsonProcessingException {

		//TODO: enrich tests for required fields
		JsonNode prevJson = mapper.readValue(previousSchema, JsonNode.class);
		JsonNode newJson = mapper.readValue(newSchema, JsonNode.class);
		Integer prevDeprecated = 0;
		Integer newDeprecated = 0;

		List<String> newKeys = new ArrayList<>();
		for (Iterator<Map.Entry<String, JsonNode>> it = newJson.get("properties").fields(); it.hasNext(); ) {
			Map.Entry<String, JsonNode> prop = it.next();
			if(prop.getValue().get("deprecated") != null &&  prop.getValue().get("deprecated").asBoolean()){
				newDeprecated++;
			}
			List<String> propsList = getTypesList(prop);
			newKeys.addAll(propsList);
		}

		List<String> prevKeys = new ArrayList<>();
		for (Iterator<Map.Entry<String, JsonNode>> it = prevJson.get("properties").fields(); it.hasNext(); ) {
			Map.Entry<String, JsonNode> prop = it.next();
			if(prop.getValue().get("deprecated") != null &&  prop.getValue().get("deprecated").asBoolean()){
				prevDeprecated++;
			}
			List<String> propsList = getTypesList(prop);
			prevKeys.addAll(propsList);
		}

		List<String>missingFromNewSchema = prevKeys.stream()
				.filter(s -> !newKeys.contains(s))
				.collect(Collectors.toList());

		if (missingFromNewSchema.size() > 0){
			throw new JoyceSchemaEngineException(String.format("New Schema is not valid some key were deleted or type changed %s", String.join(", ", missingFromNewSchema)));
		}

		return newDeprecated > prevDeprecated;
	}

	private List<String> getTypesList(Map.Entry<String, JsonNode> stringJsonNodeEntry) {
		List<String> list = new ArrayList<>();
		if (stringJsonNodeEntry.getValue().get("type").getNodeType().equals(JsonNodeType.ARRAY)){
			for (JsonNode node : stringJsonNodeEntry.getValue().get("type")){
				list.add(stringJsonNodeEntry.getKey() +"-"+ node.asText());
			}
		} else {
			list.add(stringJsonNodeEntry.getKey() +"-"+ stringJsonNodeEntry.getValue().get("type").asText());
		}
		return list;
	}


}
