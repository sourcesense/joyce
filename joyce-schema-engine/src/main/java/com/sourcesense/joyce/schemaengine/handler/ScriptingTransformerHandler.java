package com.sourcesense.joyce.schemaengine.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sourcesense.joyce.schemaengine.annotation.SchemaTransformationHandler;
import com.sourcesense.joyce.schemaengine.enumeration.ScriptingEngine;
import com.sourcesense.joyce.schemaengine.exception.JoyceSchemaEngineException;
import com.sourcesense.joyce.schemaengine.model.SchemaEngineContext;
import com.sourcesense.joyce.schemaengine.model.handler.ScriptHandlerArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@SchemaTransformationHandler(keyword = "script")
public class ScriptingTransformerHandler implements SchemaTransformerHandler {

	private final ObjectMapper mapper;
	private final ApplicationContext applicationContext;

	/**
	 * This method evaluates a script using the right scripting service based on language.
	 *
	 * @param key     Schema property key
	 * @param args    Schema property value (contains info needed to run the script)
	 * @param context Object containg data
	 * @return Result of script processing
	 */
	@Override
	public JsonNode process(String key, String type, JsonNode args, SchemaEngineContext context) {
		ScriptHandlerArgs scriptHandlerArgs = mapper.convertValue(args, ScriptHandlerArgs.class);
		return ScriptingEngine.getScriptingServiceClass(scriptHandlerArgs.getLanguage())
				.map(applicationContext::getBean)
				.orElseThrow(() -> new JoyceSchemaEngineException(String.format(
						"No available scripting service for language '%s' for field '%s'",
						scriptHandlerArgs.getLanguage(), key)))
				.eval(key, scriptHandlerArgs, context);
	}
}
