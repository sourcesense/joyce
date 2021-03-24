package com.sourcesense.nile.schemaengine.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationResult;
import com.sourcesense.nile.schemaengine.exceptions.HandlerBeanNameNotFound;
import com.sourcesense.nile.schemaengine.exceptions.HandlerKeyMustStartWithDollarException;
import com.sourcesense.nile.schemaengine.exceptions.SchemaIsNotValidException;
import com.sourcesense.nile.schemaengine.handlers.TransormerHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
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
	private ApplicationContext context;

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

		TransormerHandler pathHandler = context.getBean("jsonPathTransformerHandler", TransormerHandler.class);
		this.registerHandler("$path", pathHandler);
		for (String key : this.properties.getHandlers().keySet()){
			TransormerHandler handler = context.getBean(this.properties.getHandlers().get(key), TransormerHandler.class);
			if (handler == null){
				throw new HandlerBeanNameNotFound(String.format("%s not found", this.properties.getHandlers().get(key)));
			}
			this.registerHandler(key, handler);
		}
	}

	/**
	 * Register a new transformation handler for the given Key
	 * @param key a key that MUST start with a dollar sign
	 * @param handler an instance implementing TransformationHandler
	 */
	public void registerHandler(String key, TransormerHandler handler) throws HandlerKeyMustStartWithDollarException {
		if(!key.startsWith("$")){
			throw new HandlerKeyMustStartWithDollarException(String.format("%s does not start with a dollar", key));
		}
		this.handlers.put(key, handler);
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
	 * with schema and source provided as string
	 *
	 * @param jsonSchema
	 * @param sourceJson
	 * @return
	 */
	public JsonNode process(String jsonSchema, String sourceJson) throws JsonProcessingException {
		JsonNode schemaJsonNode = yamlReader.readValue(jsonSchema, JsonNode.class);
		JsonNode sourceJsonNode = mapper.readValue(sourceJson, JsonNode.class);
		return process(schemaJsonNode, sourceJsonNode);
	}

	/**
	 * Create a new json described by the json-schema provided, with sourceJson as input for the transformation
	 *
	 * @param schemaJsonNode
	 * @param sourceJsonNode
	 * @return
	 */
	private JsonNode process(JsonNode schemaJsonNode, JsonNode sourceJsonNode) {
		JsonSchema schema = factory.getSchema(schemaJsonNode);
		Optional<Map<String, Object>> context = getContext(schema.getSchemaNode());
		JsonNode result = this.parse(null, schema.getSchemaNode(), sourceJsonNode);

		ValidationResult validation = schema.validateAndCollect(result);
		if(validation.getValidationMessages().size() > 0){
			throw new SchemaIsNotValidException(validation);
		}
		return result;
	}

	private JsonNode parse(String key, JsonNode schema, JsonNode sourceJsonNode) {

		if (schema.getNodeType().equals(JsonNodeType.OBJECT)){
			// Apply custom handlers
			sourceJsonNode = this.applyHandlers(key, schema, sourceJsonNode);

			String type = schema.get("type").asText();
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


	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.context = applicationContext;
	}
}
