package com.sourcesense.joyce.schemaengine.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sourcesense.joyce.schemaengine.annotation.SchemaTransformationHandler;
import lombok.extern.slf4j.Slf4j;
import org.python.jsr223.PyScriptEngine;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@SchemaTransformationHandler(keyword = "$pyExpr")
public class PythonScriptingTransformerHandler extends ScriptingTransformerHandler<PyScriptEngine> {

	public PythonScriptingTransformerHandler(
			PyScriptEngine scriptEngine,
			@Qualifier("secondaryJsonMapper") ObjectMapper mapper) {

		super(scriptEngine, mapper);
	}

	@Override
	protected String getScriptingFunction(JsonNode script) {
		return null;
	}
}
