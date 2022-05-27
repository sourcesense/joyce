package com.sourcesense.joyce.schemaengine.service.scripting;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sourcesense.joyce.schemaengine.exception.JoyceSchemaEngineException;
import com.sourcesense.joyce.schemaengine.model.dto.SchemaEngineContext;
import com.sourcesense.joyce.schemaengine.model.dto.handler.ScriptHandlerArgs;
import lombok.RequiredArgsConstructor;

import javax.script.*;

@RequiredArgsConstructor
public abstract class ScriptingService {

	private final ObjectMapper jsonMapper;
	private final ScriptEngine scriptEngine;

	/**
	 * This method builds the function invoked to process one line script.
	 * Every language will have his own function.
	 *
	 * @param script Script code
	 * @return Function used to process one line script
	 */
	protected abstract String getOneLineScriptingFunction(String script);

	/**
	 * This method builds the function invoked to process multiline script.
	 * Every language will have his own function.
	 *
	 * @param script Script code
	 * @return Function used to process multiline script
	 */
	protected abstract String getMultilineScriptingFunction(String script);

	/**
	 * This method uses script engine to invoke a function that will run a script.
	 *
	 * @param key               Schema property key
	 * @param scriptHandlerArgs All is needed to run the script
	 * @param context           Context
	 * @return Result of script processing
	 */
	public JsonNode eval(String key, ScriptHandlerArgs scriptHandlerArgs, SchemaEngineContext context) {
		try {
			scriptEngine.setContext(new SimpleScriptContext());
			scriptEngine.eval(this.computeScript(scriptHandlerArgs));

			Invocable invocableEngine = (Invocable) scriptEngine;

			return jsonMapper.readTree(
					(String) invocableEngine.invokeFunction(
							"executeScript",
							jsonMapper.writeValueAsString(context)
					));
		} catch (Exception exception) {
			throw new JoyceSchemaEngineException(String.format(
					"Error happened while executing script for field '%s', error message is '%s'",
					key, exception.getMessage()
			));
		}
	}

	private String computeScript(ScriptHandlerArgs scriptHandlerArgs) {
		return scriptHandlerArgs.isOneLine()
				? this.getOneLineScriptingFunction(scriptHandlerArgs.getCode())
				: this.getMultilineScriptingFunction(scriptHandlerArgs.getCode());
	}
}
