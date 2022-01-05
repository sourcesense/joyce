package com.sourcesense.joyce.core.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sourcesense.joyce.core.model.SchemaEntity;
import com.sourcesense.joyce.schemaengine.handler.SchemaTransformerHandler;
import com.sourcesense.joyce.schemaengine.service.SchemaEngine;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Map;


//Todo: remember me
@Service
public class EnhancedSchemaEngine extends SchemaEngine {

	public EnhancedSchemaEngine(
			@Qualifier("jsonMapper") ObjectMapper jsonMapper,
			@Qualifier("transformerHandlers") Map<String, SchemaTransformerHandler> transformerHandlers) {

		super(jsonMapper, transformerHandlers);
	}

	public JsonNode process(SchemaEntity schema, JsonNode source, Object context) {
		JsonNode jsonSchema = jsonMapper.valueToTree(schema);
		return super.process(jsonSchema, source, context);
	}

	public void validate(SchemaEntity schema, JsonNode content) {
		JsonNode jsonSchema = jsonMapper.valueToTree(schema);
		super.validate(jsonSchema, content);
	}
}
