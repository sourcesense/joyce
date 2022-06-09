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
import com.sourcesense.joyce.schemaengine.exception.InvalidSchemaException;
import com.sourcesense.joyce.schemaengine.exception.JoyceSchemaEngineException;
import com.sourcesense.joyce.schemaengine.handler.SchemaTransformerHandler;
import com.sourcesense.joyce.schemaengine.model.dto.JoyceMetaSchema;
import com.sourcesense.joyce.schemaengine.model.dto.SchemaEngineContext;
import com.sourcesense.joyce.schemaengine.model.dto.schema.SchemaPropertyHandler;
import com.sourcesense.joyce.schemaengine.model.dto.schema.SchemaPropertyHandlers;
import com.sourcesense.joyce.schemaengine.templating.handlebars.resolver.HandlebarsTemplateResolver;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class SchemaEngine<T> {

	public static final String SCHEMA = "$schema";
	public static final String UID = "uid";
	public static final String DATA = "data";
	public static final String METADATA = "metadata";

	public static final String TYPE = "type";
	public static final String VALUE = "value";
	public static final String APPLY = "apply";
	public static final String ITEMS = "items";
	public static final String PROPERTIES = "properties";

	protected final ObjectMapper jsonMapper;
	protected final HandlebarsTemplateResolver handlebarsTemplateResolver;
	protected final Map<String, SchemaTransformerHandler> transformerHandlers;

	protected JsonSchemaFactory factory;

	public SchemaEngine(
			ObjectMapper jsonMapper,
			HandlebarsTemplateResolver handlebarsTemplateResolver,
			@Qualifier("transformerHandlers") Map<String, SchemaTransformerHandler> transformerHandlers) {

		this.jsonMapper = jsonMapper;
		this.transformerHandlers = transformerHandlers;
		this.handlebarsTemplateResolver = handlebarsTemplateResolver;
		this.factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7);
	}

	/**
	 * Default initialization of the Engine when initialized by Spring
	 */
	@PostConstruct
	public void defaultInit() {
		this.registerMetaSchema();
	}

	/**
	 * Generates the Meta Schema with correct registered Keywords
	 */
	protected void registerMetaSchema() {
		JsonMetaSchema metaSchema = JoyceMetaSchema.getInstance(transformerHandlers.keySet());
		this.factory = new JsonSchemaFactory.Builder()
				.defaultMetaSchemaURI(metaSchema.getUri())
				.addMetaSchema(metaSchema)
				.build();
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
		JsonNode schemaJsonNode = jsonMapper.readValue(jsonSchema, JsonNode.class);
		JsonNode sourceJsonNode = jsonMapper.readValue(sourceJson, JsonNode.class);
		return process(schemaJsonNode, sourceJsonNode);
	}

	/**
	 * Utility method
	 *
	 * @param jsonSchema
	 * @param sourceJson
	 * @return
	 * @throws JsonProcessingException
	 */
	public JsonNode process(String jsonSchema, Map<?, ?> sourceJson) throws JsonProcessingException {
		JsonNode schemaJsonNode = jsonMapper.readValue(jsonSchema, JsonNode.class);
		JsonNode sourceJsonNode = jsonMapper.convertValue(sourceJson, JsonNode.class);
		return process(schemaJsonNode, sourceJsonNode);
	}

	public JsonNode process(Map<?, ?> jsonSchema, Map<?, ?> sourceJson) {
		JsonNode schemaJsonNode = jsonMapper.convertValue(jsonSchema, JsonNode.class);
		JsonNode sourceJsonNode = jsonMapper.convertValue(sourceJson, JsonNode.class);
		return process(schemaJsonNode, sourceJsonNode);
	}

	public JsonNode process(T schema, JsonNode source) {
		return this.process(schema, source, null);
	}

	public JsonNode process(T schema, JsonNode source, Object context) {
		JsonNode jsonSchema = jsonMapper.valueToTree(schema);
		return this.process(jsonSchema, source, context);
	}

	public JsonNode process(JsonNode schema, JsonNode source) {
		return this.process(schema, source, null);
	}

	/**
	 * Create a new json described by the json-schema provided, with sourceJson as input for the transformation
	 *
	 * @param schema
	 * @param source
	 * @return
	 */
	protected JsonNode process(JsonNode schema, JsonNode source, Object extra) {

		JsonNode jsonSchema = factory.getSchema(schema).getSchemaNode();
		JsonNode data = this.computeData(schema, source, extra);
		JsonNode metadata = this.computeMetadataWithoutData(jsonSchema.get(METADATA));

		SchemaEngineContext internalContext = SchemaEngineContext.builder()
				.src(source)
				.data(data)
				.metadata(metadata)
				.out(source)
				.extra(extra)
				.build();

		JsonNode result = this.parse(null, jsonSchema, internalContext);

		this.validate(schema, result);
		return result;
	}

	public void validate(T schema, JsonNode content) {
		JsonNode jsonSchema = jsonMapper.valueToTree(schema);
		this.validate(jsonSchema, content);
	}

	/**
	 * Validate a document against a json schema
	 * throws if it is not valid
	 *
	 * @param schema
	 * @param content
	 */
	public void validate(JsonNode schema, JsonNode content) {
		JsonSchema jsonSchema = factory.getSchema(schema);
		ValidationResult validation = jsonSchema.validateAndCollect(content);
		if (validation.getValidationMessages().size() > 0) {
			throw new InvalidSchemaException(validation);
		}
	}

	public void checkForBreakingChanges(T existingSchema, T actualSchema) {
		this.checkForBreakingChanges(
				jsonMapper.valueToTree(existingSchema),
				jsonMapper.valueToTree(actualSchema)
		);
	}

	/**
	 * Detects if the changes in the schema are breaking changes or not, throws Exception if the schema changes are unacceptable
	 *
	 * @param prevJson
	 * @param newJson
	 * @return
	 */
	public Boolean checkForBreakingChanges(JsonNode prevJson, JsonNode newJson) {

		//TODO: enrich tests for required fields

		List<String> newKeys = new ArrayList<>();
		Integer newDeprecated = this.computeDeprecatedCount(newJson, newKeys);

		List<String> prevKeys = new ArrayList<>();
		Integer prevDeprecated = this.computeDeprecatedCount(prevJson, prevKeys);

		List<String> missingFromNewSchema = prevKeys.stream()
				.filter(s -> !newKeys.contains(s))
				.collect(Collectors.toList());

		if (missingFromNewSchema.size() > 0) {
			throw new JoyceSchemaEngineException(String.format(
					"New Schema is not valid some key were deleted or type changed %s",
					String.join(", ", missingFromNewSchema))
			);
		}
		return newDeprecated > prevDeprecated;
	}

	protected Integer computeDeprecatedCount(
			JsonNode json,
			List<String> keys) {

		Integer deprecated = 0;
		for (Iterator<Map.Entry<String, JsonNode>> it = json.get("properties").fields(); it.hasNext(); ) {
			Map.Entry<String, JsonNode> prop = it.next();
			if (prop.getValue().get("deprecated") != null && prop.getValue().get("deprecated").asBoolean()) {
				deprecated++;
			}
			List<String> propsList = getTypesList(prop);
			keys.addAll(propsList);
		}
		return deprecated;
	}

	/**
	 * Main Parse method, applies the transformer and navigate the schema calling parse recursively
	 * Root key could be null
	 * the schema is the json-schema definition
	 * source is the json source node from wich the transformation are applied
	 *
	 * @param key
	 * @param schema
	 * @param context
	 * @return
	 */
	protected JsonNode parse(String key, JsonNode schema, SchemaEngineContext context) {
		if (!JsonNodeType.OBJECT.equals(schema.getNodeType())) {
			throw new JoyceSchemaEngineException(String.format(
					"Cannot parse schema '%s', type is not object",
					schema.get(UID)
			));
		}
		try {
			// Apply custom handlers
			Optional<JsonNode> transformed = this.applyHandlers(key, schema, context);
			if (transformed.isPresent()) {
				ObjectNode node = jsonMapper.createObjectNode();
				node.set(key, transformed.get());

				ObjectNode tempSchema = schema.deepCopy();
				tempSchema.remove(VALUE);
				tempSchema.remove(APPLY);

				JsonNode transformedNode = "object".equals(schema.get(TYPE).asText())
						? transformed.get()
						: node;

				return this.parse(key, tempSchema, context.withUpdatedOutput(transformedNode));
			}
			JsonNode type = Optional.ofNullable(schema.get(TYPE)).orElse(new TextNode("string"));
			if (type.getNodeType().equals(JsonNodeType.ARRAY)) {
				for (JsonNode aType : type) {
					JsonNode result = this.parseType(aType.asText(), key, schema, context);
					if (result != null) {
						return result;
					}
				}
				return null;
			} else {
				return parseType(type.asText(), key, schema, context);
			}
		} catch (Exception e) {
			throw new JoyceSchemaEngineException(String.format(
					"Cannot parse [%s]: %s", key, e.getMessage()
			));
		}
	}

	protected JsonNode parseType(String type, String key, JsonNode schema, SchemaEngineContext context) {
		if ("object".equals(type)) {
			ObjectNode objectNode = jsonMapper.createObjectNode();
			JsonNode props = schema.get(PROPERTIES);
			if (props != null) {
				Iterator<Map.Entry<String, JsonNode>> iter = props.fields();
				while (iter.hasNext()) {
					Map.Entry<String, JsonNode> entry = iter.next();
					JsonNode node = this.parse(entry.getKey(), entry.getValue(), context);
					objectNode.set(entry.getKey(), node);
				}
			}
			return objectNode;
		} else if ("array".equals(type)) {
			ArrayNode arrayNode = jsonMapper.createArrayNode();
			for (JsonNode item : context.getOut().get(key)) {
				JsonNode parsedItem = this.parse(null, schema.get(ITEMS), context.withUpdatedOutput(item));
				if (!parsedItem.isNull()) {
					arrayNode.add(parsedItem);
				}
			}
			return arrayNode;
		} else {
			JsonNode result = StringUtils.isNotEmpty(key) ? context.getOut().get(key) : context.getOut();
			if (result == null) {
				return null;
			}
			switch (type) {
				case "integer":
					return result.asText().isEmpty() ? null : JsonNodeFactory.instance.numberNode(Integer.parseInt(result.asText()));
				case "number":
					return result.asText().isEmpty() ? null : JsonNodeFactory.instance.numberNode(Double.parseDouble(result.asText()));
				case "null":
					return null;
				default:
					//TODO: handle other types and modification ie range and other limitations
					return result;
			}
		}
	}

	/**
	 * Apply registered handlers in cascade sing output from the first as input to the following
	 *
	 * @param key
	 * @param schema
	 * @param context
	 * @return
	 */
	protected Optional<JsonNode> applyHandlers(String key, JsonNode schema, SchemaEngineContext context) {
		// Apply handlers in cascade
		SchemaPropertyHandlers propertyHandlers = jsonMapper.convertValue(schema, SchemaPropertyHandlers.class);
		if(Strings.isEmpty(propertyHandlers.getValue()) && propertyHandlers.getApply().isEmpty()) {
			return Optional.empty();
		}
		Optional<SchemaPropertyHandler> valueHandler = this.computeValueHandler(propertyHandlers);
		List<SchemaPropertyHandler> knownHandlers = Stream.concat(valueHandler.stream(), propertyHandlers.getApply().stream())
				.filter(propertyHandler -> transformerHandlers.containsKey(propertyHandler.getHandler()))
				.collect(Collectors.toList());

		if (knownHandlers.size() < 1) {
			return Optional.empty();
		}
		JsonNode returnNode = context.getOut().deepCopy();
		for (SchemaPropertyHandler knownHandler : knownHandlers) {

			JsonNode handlerArgs = handlebarsTemplateResolver.resolve(knownHandler.getArgs(), context);
			SchemaTransformerHandler handler = transformerHandlers.get(knownHandler.getHandler());
			returnNode = handler.process(
					key, propertyHandlers.getType().asText(),
					handlerArgs, context.withUpdatedOutput(returnNode)
			);
		}
		return Optional.ofNullable(returnNode);
	}

	protected List<String> getTypesList(Map.Entry<String, JsonNode> stringJsonNodeEntry) {
		List<String> list = new ArrayList<>();
		if (stringJsonNodeEntry.getValue().get(TYPE).getNodeType().equals(JsonNodeType.ARRAY)) {
			for (JsonNode node : stringJsonNodeEntry.getValue().get(TYPE)) {
				list.add(stringJsonNodeEntry.getKey() + "-" + node.asText());
			}
		} else {
			list.add(stringJsonNodeEntry.getKey() + "-" + stringJsonNodeEntry.getValue().get(TYPE).asText());
		}
		return list;
	}

	private JsonNode computeData(JsonNode jsonSchema, JsonNode source, Object extra) {
		return Optional.of(jsonSchema)
				.map(schema -> schema.get(METADATA))
				.map(meta -> meta.get(DATA))
				.filter(Predicate.not(JsonNode::isEmpty))
				.filter(JsonNode::isObject)
				.map(ObjectNode.class::cast)
				.map(ObjectNode::deepCopy)
				.map(dataSchema -> dataSchema.put(SCHEMA, jsonSchema.path(SCHEMA).asText()))
				.map(dataSchema -> this.process(dataSchema, source, extra))
				.orElseGet(jsonMapper::createObjectNode);
	}

	private JsonNode computeMetadataWithoutData(JsonNode metadata) {
		if(Objects.isNull(metadata) || metadata.isEmpty()) {
			return jsonMapper.createObjectNode();

		} else if(metadata.has(DATA)) {
			ObjectNode metadataWithoutData = metadata.deepCopy();
			metadataWithoutData.remove(DATA);
			return metadataWithoutData;

		} else {
			return metadata;
		}
	}

	private Optional<SchemaPropertyHandler> computeValueHandler(SchemaPropertyHandlers propertyHandlers) {
		return Optional.ofNullable(propertyHandlers.getValue())
				.filter(Strings::isNotEmpty)
				.map(value -> SchemaPropertyHandler.builder()
						.handler("extract")
						.args(new TextNode(value))
						.build()
				);
	}
}
