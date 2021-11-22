package com.sourcesense.joyce.schemaengine.service.scripting;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sourcesense.joyce.schemaengine.exception.JoyceSchemaEngineException;
import com.sourcesense.joyce.schemaengine.model.handler.ScriptHandlerData;
import lombok.RequiredArgsConstructor;

import javax.script.*;
import java.util.Optional;

@RequiredArgsConstructor
public abstract class ScriptingService {

	private final ObjectMapper mapper;
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
	 *
	 * This method uses script engine to invoke a function that will run a script.
	 *
	 * @param key Schema property key
	 * @param scriptHandlerData All is needed to run the script
	 * @param source Source
	 * @param metadata Metadata
	 * @param context Context
	 * @return Result of script processing
	 */
	public JsonNode eval(
			String key,
			ScriptHandlerData scriptHandlerData,
			JsonNode source,
			Optional<JsonNode> metadata,
			Optional<Object> context) {

		try {
			scriptEngine.setContext(new SimpleScriptContext());
			scriptEngine.eval(this.computeScript(scriptHandlerData));

			Invocable invocableEngine = (Invocable) scriptEngine;

			return mapper.readTree(
					(String) invocableEngine.invokeFunction(
							"executeScript",
							mapper.writeValueAsString(source),
							this.writeValueAsString(metadata),
							this.writeValueAsString(context)
					));
		} catch (Exception exception) {
			throw new JoyceSchemaEngineException(exception.getMessage());
		}
	}

	private String computeScript(ScriptHandlerData scriptHandlerData) {
		return scriptHandlerData.isOneLine()
				? this.getOneLineScriptingFunction(scriptHandlerData.getCode())
				: this.getMultilineScriptingFunction(scriptHandlerData.getCode());
	}

	private <T> String writeValueAsString(Optional<T> value) throws JsonProcessingException {
		return value.isPresent()
				? mapper.writeValueAsString(value.get())
				: "{}";
	}
}
