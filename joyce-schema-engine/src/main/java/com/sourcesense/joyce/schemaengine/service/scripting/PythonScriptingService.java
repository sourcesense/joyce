package com.sourcesense.joyce.schemaengine.service.scripting;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.python.jsr223.PyScriptEngine;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.stream.Collectors;

@Service
public class PythonScriptingService extends ScriptingService {

	public PythonScriptingService(
			ObjectMapper mapper,
			PyScriptEngine scriptEngine) {

		super(mapper, scriptEngine);
	}

	@Override
	protected String getOneLineScriptingFunction(String scriptCode) {
		return this.buildScriptFunction(
				"result = " + scriptCode + "\n"
		);
	}

	@Override
	protected String getMultilineScriptingFunction(String scriptCode) {
		String cleanedMultiline = Arrays.stream(scriptCode.split("\n"))
				.collect(Collectors.joining("\n\t\t"));
		return this.buildScriptFunction(String.format(
				"def wrapper(source, metadata, context):\n" +
				"\t\t%s\n" +
				"\tresult = wrapper(source, metadata, context)\n", cleanedMultiline));
	}

	private String buildScriptFunction(String scriptResult) {
		return "import json\n" +
				"def executeScript(__source, __metadata, __context):\n" +
				"\tsource = json.loads(__source)\n" +
				"\tmetadata = json.loads(__metadata)\n" +
				"\tcontext = json.loads(__context)\n" +
				"\t" + scriptResult +
				"\treturn json.dumps(result)\n";
	}
}
