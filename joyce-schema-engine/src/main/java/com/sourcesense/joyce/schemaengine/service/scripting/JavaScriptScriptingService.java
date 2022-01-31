package com.sourcesense.joyce.schemaengine.service.scripting;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.oracle.truffle.js.scriptengine.GraalJSScriptEngine;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class JavaScriptScriptingService extends ScriptingService {

	public JavaScriptScriptingService(
			ObjectMapper jsonMapper,
			GraalJSScriptEngine scriptEngine) {

		super(jsonMapper, scriptEngine);
	}

	@Override
	protected String getOneLineScriptingFunction(String scriptCode) {
		return this.buildScriptFunction(
				String.format("const result = %s;\n", scriptCode)
		);
	}

	@Override
	protected String getMultilineScriptingFunction(String scriptCode) {
		return this.buildScriptFunction(
				String.format(
						"const executeCode = () => {\n%s\n};\n" +
						"const result = executeCode();\n",
						scriptCode
				)
		);
	}

	private String buildScriptFunction(String scriptResult) {
		return "const executeScript = (__source, __metadata, __context) => {\n" +
				"const source = JSON.parse(__source);\n" +
				"const metadata = JSON.parse(__metadata);\n" +
				"const context = JSON.parse(__context);\n" +
				scriptResult +
				"return JSON.stringify(result);\n" +
				"}";
	}
}
