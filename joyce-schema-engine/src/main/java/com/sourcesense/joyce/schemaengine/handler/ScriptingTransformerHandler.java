package com.sourcesense.joyce.schemaengine.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sourcesense.joyce.schemaengine.annotation.SchemaTransformationHandler;
import com.sourcesense.joyce.schemaengine.enumeration.ScriptingEngine;
import com.sourcesense.joyce.schemaengine.model.ScriptData;
import com.sourcesense.joyce.schemaengine.service.scripting.ScriptingService;
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

	@Override
	public JsonNode process(
			String key,
			JsonNode value,
			JsonNode source,
			Optional<JsonNode> metadata,
			Optional<Object> context) {

		ScriptData scriptData = mapper.convertValue(value, ScriptData.class);
		return ScriptingEngine
				.getScriptingService(scriptData.getLanguage(), applicationContext)
				.eval(key, scriptData, source, metadata, context);
	}
}
