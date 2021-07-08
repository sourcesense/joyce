package com.sourcesense.joyce.schemaengine.service.scripting;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sourcesense.joyce.schemaengine.exception.JoyceSchemaEngineException;
import com.sourcesense.joyce.schemaengine.model.ScriptData;
import lombok.RequiredArgsConstructor;

import javax.script.*;
import java.util.Optional;

@RequiredArgsConstructor
public abstract class ScriptingService {

	private final ObjectMapper mapper;
	private final ScriptEngine scriptEngine;

	protected abstract String getOneLineScriptingFunction(String script);

	protected abstract String getMultilineScriptingFunction(String script);

	public JsonNode eval(
			String key,
			ScriptData scriptData,
			JsonNode source,
			Optional<JsonNode> metadata,
			Optional<Object> context) {

		try {
			scriptEngine.setContext(new SimpleScriptContext());
			scriptEngine.eval(this.computeScript(scriptData));

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

	private String computeScript(ScriptData scriptData) {
		return scriptData.isOneLine()
				? this.getOneLineScriptingFunction(scriptData.getCode())
				: this.getMultilineScriptingFunction(scriptData.getCode());
	}

	private <T> String writeValueAsString(Optional<T> value) throws JsonProcessingException {
		return value.isPresent()
				? mapper.writeValueAsString(value.get())
				: "{}";
	}
}
