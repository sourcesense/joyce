package com.sourcesense.nile.schemaengine.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.networknt.schema.*;
import com.sourcesense.nile.schemaengine.NileMetaSchema;
import com.sourcesense.nile.schemaengine.dto.ProcessResult;
import com.sourcesense.nile.schemaengine.exceptions.HandlerBeanNameNotFound;
import com.sourcesense.nile.schemaengine.exceptions.InvalidSchemaException;
import com.sourcesense.nile.schemaengine.exceptions.SchemaIsNotValidException;
import com.sourcesense.nile.schemaengine.handlers.TransormerHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@Slf4j
public class SchemaEngine implements ApplicationContextAware {
	public static final String METADATA = "$metadata";
	private ObjectMapper mapper;
	private JsonSchemaFactory factory;
	private Map<String, TransormerHandler> handlers;
	private final SchemaEngineProperties properties;
	private ApplicationContext applicationContext;

	public SchemaEngine(SchemaEngineProperties properties) {
		this.factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7);
		this.mapper = new ObjectMapper();
		this.handlers = new HashMap<>();
		this.properties = properties;
	}

	/**
	 * Default initialization of the Engine when initialized by Spring
	 */
	@PostConstruct
	public void defaultInit() {

		/**
		 * Register Default Handlers
		 */
		Map<String, String> defaultHandlers = Map.of(
				"$path", "jsonPathTransformerHandler",
				"$fixed", "fixedValueTransformerHandler",
				"$meta", "metadataValueTransformerHandler"
		);
		for (String key : defaultHandlers.keySet()) {
			TransormerHandler handler = applicationContext.getBean(defaultHandlers.get(key), TransormerHandler.class);
			this.registerHandler(key, handler);
		}


		/**
		 * Register Handlers from configuration
		 */
		for (String key : this.properties.getHandlers().keySet()){
			TransormerHandler handler = applicationContext.getBean(this.properties.getHandlers().get(key), TransormerHandler.class);
			if (handler == null){
				throw new HandlerBeanNameNotFound(String.format("%s not found", this.properties.getHandlers().get(key)));
			}
			this.registerHandler(key, handler);
		}

		this.registerMetaSchema();
	}

	/**
	 * Genrates the Meta Schema with correct registered Keywords
	 */
	public void registerMetaSchema() {
		JsonMetaSchema metaSchema = NileMetaSchema.getInstance(new ArrayList<>(handlers.keySet()));
		JsonSchemaFactory.Builder builder = new JsonSchemaFactory.Builder();
		this.factory = builder.defaultMetaSchemaURI(metaSchema.getUri())
				.addMetaSchema(metaSchema).build();
	//
	}


	/**
	 * Parse the context node applying the transsformers if presents
	 * @param node
	 * @param source
	 * @return
	 */
	protected Optional<JsonNode> parseMetadata(JsonNode node, JsonNode source){
		Optional<ObjectNode> metadataNode = Optional.ofNullable((ObjectNode)node.get(METADATA));
		if(!metadataNode.isPresent()){
			return Optional.empty();
		}

		for (Iterator<Map.Entry<String, JsonNode>> it = metadataNode.get().fields(); it.hasNext(); ) {
			Map.Entry<String, JsonNode> field = it.next();
			if(field.getValue().getNodeType().equals(JsonNodeType.OBJECT)){
				Optional<JsonNode> transformed = this.applyHandlers( field.getValue(), source, Optional.empty());
				transformed.ifPresent(jsonNode -> {
					metadataNode.get().set(field.getKey(), jsonNode);
				});

			}
		}

		return Optional.of(metadataNode.get());
	}

	/**
	 * Main Parse method, applies the transformer and navigate the schema calling parse recursively
	 * Root key could be null
	 * the schema is the json-schema definition
	 * source is the json source node from wich the transformation are applied
	 * @param key
	 * @param schema
	 * @param sourceJsonNode
	 * @return
	 */
	private JsonNode parse(String key, JsonNode schema, JsonNode sourceJsonNode, Optional<JsonNode> metadata) {

		if (schema.getNodeType().equals(JsonNodeType.OBJECT)){
			// Apply custom handlers
			Optional<JsonNode> transformed = this.applyHandlers(schema, sourceJsonNode, metadata);
			transformed.ifPresent(jsonNode -> {
				((ObjectNode)sourceJsonNode).set(key, jsonNode);
			});

			String type = Optional.ofNullable(schema.get("type")).orElse(new TextNode("string")).asText();
			if(type.equals("object")){
				ObjectNode objectNode = mapper.createObjectNode();
				JsonNode props = schema.get("properties");
				if (props != null) {
					Iterator<Map.Entry<String, JsonNode>> iter = props.fields();
					while (iter.hasNext()) {
						Map.Entry<String, JsonNode> entry = iter.next();
						JsonNode node = this.parse(entry.getKey(), entry.getValue(), sourceJsonNode, metadata);
						objectNode.set(entry.getKey(), node);
					}
				}
				return objectNode;
			} else if (type.equals("array")){
				ArrayNode arrayNode = mapper.createArrayNode();
				for (JsonNode item : sourceJsonNode.get(key)){
					JsonNode parsedItem = this.parse(null, schema.get("items"), item, metadata);
					arrayNode.add(parsedItem);
				}
				return arrayNode;
			} else {
				return sourceJsonNode.get(key);
			}
		}

		return null;
	}

	/**
	 * Register a new transformation handler for the given Key
	 * @param key a key that MUST start with a dollar sign
	 * @param handler an instance implementing TransformationHandler
	 */
	public void registerHandler(String key, TransormerHandler handler) {
		if(!key.startsWith("$")){
			key = "$".concat(key);
		}
		this.handlers.put(key, handler);
	}


	/**
	 * Apply registered handlers, it stops at the first encountered
	 * TODO: find a way to choose which one is applied or apply all in a given order
	 * @param schema
	 * @param sourceJsonNode
	 * @return
	 */
	private Optional<JsonNode> applyHandlers(JsonNode schema, JsonNode sourceJsonNode, Optional<JsonNode> metadata) {

		if(sourceJsonNode.getNodeType() != JsonNodeType.OBJECT){
			return Optional.empty();
		}

		// Apply handlers in cascade
		List<String> knownHandlerKeys = StreamSupport.stream(
				Spliterators.spliteratorUnknownSize(schema.fieldNames(), Spliterator.ORDERED),
				false)
				.filter(handlers.keySet()::contains)
				.collect(Collectors.toList());
		if (knownHandlerKeys.size() < 1){
			return Optional.empty();
		}

		JsonNode returnNode = sourceJsonNode.deepCopy();
		for (String handlerKey : knownHandlerKeys){
				TransormerHandler handler = this.handlers.get(handlerKey);
				returnNode = handler.process(schema.get(handlerKey), returnNode, metadata);

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
	public ProcessResult process(String jsonSchema, String sourceJson) throws JsonProcessingException {
		JsonNode schemaJsonNode = mapper.readValue(jsonSchema, JsonNode.class);
		JsonNode sourceJsonNode = mapper.readValue(sourceJson, JsonNode.class);
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
	public ProcessResult process(String jsonSchema, Map sourceJson) throws JsonProcessingException {
		JsonNode schemaJsonNode = mapper.readValue(jsonSchema, JsonNode.class);
		JsonNode sourceJsonNode = mapper.convertValue(sourceJson, JsonNode.class);
		return process(schemaJsonNode, sourceJsonNode);
	}

	public ProcessResult process(Map jsonSchema, Map sourceJson) {
		JsonNode schemaJsonNode = mapper.convertValue(jsonSchema, JsonNode.class);
		JsonNode sourceJsonNode = mapper.convertValue(sourceJson, JsonNode.class);
		return process(schemaJsonNode, sourceJsonNode);
	}

	/**
	 * Create a new json described by the json-schema provided, with sourceJson as input for the transformation
	 *
	 * @param schema
	 * @param source
	 * @return
	 */
	public ProcessResult process(JsonNode schema, JsonNode source) {
		JsonSchema jsonSchema = factory.getSchema(schema);
		Optional<JsonNode> metadata = parseMetadata(jsonSchema.getSchemaNode(), source);
		JsonNode result = this.parse(null, jsonSchema.getSchemaNode(), source, metadata);

		ValidationResult validation = jsonSchema.validateAndCollect(result);
		if(validation.getValidationMessages().size() > 0){
			throw new SchemaIsNotValidException(validation);
		}

		return new ProcessResult(result, metadata);
	}


	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	/**
	 * Detects if the changes in the schema are breaking changes or not, throws Exception if the schema changes are unacceptable
	 * @param previousSchema
	 * @param newSchema
	 * @return
	 */
	public Boolean hasBreakingChanges(String previousSchema, String newSchema) throws JsonProcessingException {
		JsonNode prevJson = mapper.readValue(previousSchema, JsonNode.class);
		JsonNode newJson = mapper.readValue(newSchema, JsonNode.class);
		List<String> newKeys = StreamSupport.stream(
				Spliterators.spliteratorUnknownSize(newJson.get("properties").fields(), Spliterator.ORDERED), false)
				.map(stringJsonNodeEntry -> stringJsonNodeEntry.getKey() + stringJsonNodeEntry.getValue().get("type").asText())
				.collect(Collectors.toList());

		List<String> prevkeys = StreamSupport.stream(
				Spliterators.spliteratorUnknownSize(prevJson.get("properties").fields(), Spliterator.ORDERED), false)
				.map(stringJsonNodeEntry -> stringJsonNodeEntry.getKey() + stringJsonNodeEntry.getValue().get("type").asText())
				.collect(Collectors.toList());

		List<String>missingFromNewSchema = prevkeys.stream()
				.filter(s -> !newKeys.contains(s))
				.collect(Collectors.toList());

		if (missingFromNewSchema.size() > 0){
			throw new InvalidSchemaException(String.format("New Schema is not valid some key were deleted %s", String.join(", ", missingFromNewSchema)));
		}

		return newKeys.size() > prevkeys.size();
	}
}
