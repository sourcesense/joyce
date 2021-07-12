package com.sourcesense.joyce.schemaengine.service.scripting;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.python.jsr223.PyScriptEngine;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class PythonScriptingService extends ScriptingService {

	public PythonScriptingService(
			@Qualifier("permissiveJsonMapper") ObjectMapper mapper,
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
		return this.buildScriptFunction(
				"  def executeCode():\n" +
						"    return " + scriptCode + "\n" +
						"  result = executeCode()\n"
		);
	}

	private String buildScriptFunction(String scriptResult) {
		return "import json" +
				"def executeScript(__source, __metadata, __context):\n" +
				"  source = json.dumps(__source)\n" +
				"  metadata = json.dumps(__metadata)\n" +
				"  context = json.dumps(__context)\n" +
				"  " + scriptResult +
				"  return json.loads(result)\n";
	}
}
