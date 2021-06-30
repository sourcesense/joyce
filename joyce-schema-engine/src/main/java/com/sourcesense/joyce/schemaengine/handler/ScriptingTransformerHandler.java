package com.sourcesense.joyce.schemaengine.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.sourcesense.joyce.schemaengine.exception.JoyceSchemaEngineException;
import lombok.RequiredArgsConstructor;

import javax.script.AbstractScriptEngine;
import javax.script.Compilable;
import javax.script.Invocable;
import javax.script.SimpleScriptContext;
import java.util.Optional;

@RequiredArgsConstructor
public abstract class ScriptingTransformerHandler<SE extends AbstractScriptEngine & Compilable & Invocable>
		implements SchemaTransformerHandler {

	private final SE scriptEngine;
	private final ObjectMapper mapper;

	protected abstract String getScriptingFunction(JsonNode script);

	@Override
	public JsonNode process(
			String key,
			JsonNode script,
			JsonNode source,
			Optional<JsonNode> metadata,
			Optional<Object> context) {

		try {
			if (!script.getNodeType().equals(JsonNodeType.STRING)) {
				throw new JoyceSchemaEngineException("Scripting handler must contain a string.");
			}

			scriptEngine.setContext(new SimpleScriptContext());
			scriptEngine.eval(this.getScriptingFunction(script));

			return mapper.readTree(
					(String) scriptEngine.invokeFunction("executeScript",
							mapper.writeValueAsString(source),
							this.writeValueAsString(metadata),
							this.writeValueAsString(context)
					));
		} catch (Exception exception) {
			throw new JoyceSchemaEngineException(exception.getMessage());
		}
	}

	private <T> String writeValueAsString(Optional<T> value) throws JsonProcessingException {
		return value.isPresent()
				? mapper.writeValueAsString(value.get())
				: "{}";
	}
}
