package com.sourcesense.joyce.schemaengine.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sourcesense.joyce.schemaengine.annotation.SchemaTransformationHandler;
import com.sourcesense.joyce.schemaengine.enumeration.ScriptingEngine;
import com.sourcesense.joyce.schemaengine.exception.JoyceSchemaEngineException;
import com.sourcesense.joyce.schemaengine.model.handler.ScriptHandlerData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
@SchemaTransformationHandler(keyword = "$script")
public class ScriptingTransformerHandler implements SchemaTransformerHandler {

	private final ObjectMapper mapper;
	private final ApplicationContext applicationContext;

	/**
	 * This method evaluates a script using the right scripting service based on language.
	 *
	 * @param key Schema property key
	 * @param value Schema property value (contains info needed to run the script)
	 * @param source Source
	 * @param metadata Metadata
	 * @param context Context
	 * @return Result of script processing
	 */
	@Override
	public JsonNode process(
			String key,
			JsonNode value,
			JsonNode source,
			Optional<JsonNode> metadata,
			Optional<Object> context) {

		ScriptHandlerData scriptHandlerData = mapper.convertValue(value, ScriptHandlerData.class);
		return ScriptingEngine
				.getScriptingServiceClass(scriptHandlerData.getLanguage())
				.map(applicationContext::getBean)
				.orElseThrow(() -> new JoyceSchemaEngineException(
						"Impossible to retrieve scripting service from application context")
				).eval(key, scriptHandlerData, source, metadata, context);
	}
}
