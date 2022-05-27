package com.sourcesense.joyce.schemaengine.service.scripting;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.python.jsr223.PyScriptEngine;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.stream.Collectors;

@Service
public class PythonScriptingService extends ScriptingService {

	public PythonScriptingService(
			ObjectMapper jsonMapper,
			PyScriptEngine scriptEngine) {

		super(jsonMapper, scriptEngine);
	}

	@Override
	protected String getOneLineScriptingFunction(String scriptCode) {
		return this.buildScriptFunction(
				"result = " + scriptCode + "\n"
		);
	}

	@Override
	protected String getMultilineScriptingFunction(String scriptCode) {
		String cleanedMultiline = String.join("\n\t\t", scriptCode.split("\n"));

		return this.buildScriptFunction(String.format(
				"def wrapper(ctx):\n" +
				"\t\t%s\n" +
				"\tresult = wrapper(ctx)\n",
				cleanedMultiline
		));
	}

	private String buildScriptFunction(String scriptResult) {
		return "import json\n" +
				"def executeScript(__ctx):\n" +
				"\tctx = json.loads(__ctx)\n" +
				"\t" + scriptResult +
				"\treturn json.dumps(result)\n";
	}
}
