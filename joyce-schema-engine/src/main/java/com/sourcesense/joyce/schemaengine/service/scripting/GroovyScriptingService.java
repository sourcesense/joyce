package com.sourcesense.joyce.schemaengine.service.scripting;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.codehaus.groovy.jsr223.GroovyScriptEngineImpl;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class GroovyScriptingService extends ScriptingService {

	public GroovyScriptingService(
			@Qualifier("permissiveJsonMapper") ObjectMapper mapper,
			GroovyScriptEngineImpl scriptEngine) {

		super(mapper, scriptEngine);
	}

	@Override
	protected String getOneLineScriptingFunction(String scriptCode) {
		return this.buildScriptFunction(
				String.format("def result = %s;\n", scriptCode)
		);
	}

	@Override
	protected String getMultilineScriptingFunction(String scriptCode) {
		return this.buildScriptFunction(
				String.format(
						"def executeCode = { -> \n%s\n};\n" +
						"def result = executeCode.call();\n",
						scriptCode
				)
		);
	}

	private String buildScriptFunction(String scriptResult) {
		return "import groovy.json.*;\n" +
				"def executeScript(__source, __metadata, __context) {\n" +
				"def jsonSlurper = new JsonSlurper();\n" +
				"def source = jsonSlurper.parseText(__source);\n" +
				"def metadata = jsonSlurper.parseText(__metadata);\n" +
				"def context = jsonSlurper.parseText(__context);\n" +
				scriptResult +
				"return JsonOutput.toJson(result);\n" +
				"}\n";
	}
}
