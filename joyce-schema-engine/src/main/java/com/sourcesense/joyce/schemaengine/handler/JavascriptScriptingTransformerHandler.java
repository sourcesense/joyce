package com.sourcesense.joyce.schemaengine.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.TextNode;
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
	protected String getScriptingFunction(JsonNode script) {
		return "const executeScript = function(__source, __metadata, __context) {\n" +
				"const source = JSON.parse(__source);\n" +
				"const metadata = JSON.parse(__metadata);\n" +
				"const context = JSON.parse(__context);\n" +
				"const result = " + script.asText() + ";\n" +
				"return JSON.stringify(result);\n" +
				"}";
	}
}
