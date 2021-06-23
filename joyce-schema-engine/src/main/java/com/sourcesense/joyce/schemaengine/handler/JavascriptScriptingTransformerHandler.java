package com.sourcesense.joyce.schemaengine.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.oracle.truffle.js.scriptengine.GraalJSScriptEngine;
import com.sourcesense.joyce.schemaengine.annotation.SchemaTransformationHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@SchemaTransformationHandler(keyword = "$jsExpr")
public class JavascriptScriptingTransformerHandler extends ScriptingTransformerHandler<GraalJSScriptEngine> {


	public JavascriptScriptingTransformerHandler(
			GraalJSScriptEngine scriptEngine,
			@Qualifier("secondaryJsonMapper") ObjectMapper mapper) {

		super(scriptEngine, mapper);
	}


	@Override
	protected String computePlaceholderWithJsonParser(String placeholder) {
		return "JSON.parse(" + placeholder + ")";
	}
}
