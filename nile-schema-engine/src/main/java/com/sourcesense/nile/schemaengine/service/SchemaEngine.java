package com.sourcesense.nile.schemaengine.service;

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
import com.networknt.schema.ValidationResult;

import com.sourcesense.nile.schemaengine.dto.ProcessResult;
import com.sourcesense.nile.schemaengine.exceptions.HandlerBeanNameNotFound;
import com.sourcesense.nile.schemaengine.exceptions.SchemaIsNotValidException;
import com.sourcesense.nile.schemaengine.handlers.TransormerHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
public class SchemaEngine implements ApplicationContextAware {
	protected static String CONTEXT_KEY = "$context";
	private ObjectMapper yamlReader;
	private ObjectMapper mapper;
	private JsonSchemaFactory factory;
	private Map<String, TransormerHandler> handlers;
	private final SchemaEngineProperties properties;
	private ApplicationContext applicationContext;

	public SchemaEngine(SchemaEngineProperties properties) {
		this.factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7);
		this.yamlReader = new ObjectMapper(new YAMLFactory());
		this.mapper = new ObjectMapper();
		this.handlers = new HashMap<>();
		this.properties = properties;
	}

	/**
	 * Default initialization of the Engine when initialized by Spring
	 */
	@PostConstruct
	public void defaultInit() {

		TransormerHandler pathHandler = applicationContext.getBean("jsonPathTransformerHandler", TransormerHandler.class);
		this.registerHandler("$path", pathHandler);
		for (String key : this.properties.getHandlers().keySet()){
			TransormerHandler handler = applicationContext.getBean(this.properties.getHandlers().get(key), TransormerHandler.class);
			if (handler == null){
				throw new HandlerBeanNameNotFound(String.format("%s not found", this.properties.getHandlers().get(key)));
			}
			this.registerHandler(key, handler);
		}
	}

	/**
	 * Parse the context node applying the transsformers if presents
	 * @param node
	 * @param source
	 * @return
	 */
	protected Optional<Map<String, Object>> parseContext(JsonNode node, JsonNode source){
		Optional<ObjectNode> contextNode = Optional.ofNullable((ObjectNode)node.get(CONTEXT_KEY));
		if(!contextNode.isPresent()){
			return Optional.empty();
		}

		for (Iterator<Map.Entry<String, JsonNode>> it = contextNode.get().fields(); it.hasNext(); ) {
			Map.Entry<String, JsonNode> field = it.next();
			if(field.getValue().getNodeType().equals(JsonNodeType.OBJECT)){
				JsonNode transformed = this.applyHandlers(field.getKey(), field.getValue(), source);
				contextNode.get().set(field.getKey(), transformed.get(field.getKey()));
			}
		}

		return Optional.of(mapper.convertValue(contextNode.get(), new TypeReference<>() {
		}));
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
	private JsonNode parse(String key, JsonNode schema, JsonNode sourceJsonNode) {

		if (schema.getNodeType().equals(JsonNodeType.OBJECT)){
			// Apply custom handlers
			sourceJsonNode = this.applyHandlers(key, schema, sourceJsonNode);

			String type = Optional.ofNullable(schema.get("type")).orElse(new TextNode("string")).asText();
			if(type.equals("object")){
				ObjectNode objectNode = mapper.createObjectNode();
				JsonNode props = schema.get("properties");
				if (props != null) {
					Iterator<Map.Entry<String, JsonNode>> iter = props.fields();
					while (iter.hasNext()) {
						Map.Entry<String, JsonNode> entry = iter.next();
						JsonNode node = this.parse(entry.getKey(), entry.getValue(), sourceJsonNode);
						objectNode.set(entry.getKey(), node);
					}
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
	 * @param key
	 * @param schema
	 * @param sourceJsonNode
	 * @return
	 */
	private JsonNode applyHandlers(String key, JsonNode schema, JsonNode sourceJsonNode) {
		TransormerHandler handler = null;
		JsonNode value = null;
		for (String handlerKey : this.handlers.keySet()){
			if(schema.has(handlerKey)){
				handler = this.handlers.get(handlerKey);
				value = schema.get(handlerKey);
				break;
			}
		}
		if (handler != null) {
			return handler.process(key, value, sourceJsonNode);
		} else {
			return sourceJsonNode;
		}
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
		JsonNode schemaJsonNode = yamlReader.readValue(jsonSchema, JsonNode.class);
		JsonNode sourceJsonNode = mapper.readValue(sourceJson, JsonNode.class);
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
		Optional<Map<String, Object>> context = parseContext(jsonSchema.getSchemaNode(), source);
		JsonNode result = this.parse(null, jsonSchema.getSchemaNode(), source);

		ValidationResult validation = jsonSchema.validateAndCollect(result);
		if(validation.getValidationMessages().size() > 0){
			throw new SchemaIsNotValidException(validation);
		}
		return new ProcessResult(result, context);
	}


	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}
}
