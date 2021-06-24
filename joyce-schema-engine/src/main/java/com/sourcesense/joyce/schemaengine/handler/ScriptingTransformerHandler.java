package com.sourcesense.joyce.schemaengine.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.sourcesense.joyce.schemaengine.exception.JoyceSchemaEngineException;
import lombok.RequiredArgsConstructor;

import javax.script.AbstractScriptEngine;
import javax.script.ScriptContext;
import javax.script.SimpleBindings;
import javax.script.SimpleScriptContext;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RequiredArgsConstructor
public abstract class ScriptingTransformerHandler<SE extends AbstractScriptEngine>
		implements SchemaTransformerHandler {

	private final static String SOURCE_PLACEHOLDER = "__source";
	private final static String METADATA_PLACEHOLDER = "__metadata";
	private final static String CONTEXT_PLACEHOLDER = "__context";

	private final SE scriptEngine;
	private final ObjectMapper mapper;

	protected abstract String computeJsonParsingPlaceholder(String field);

	@Override
	public JsonNode process(
			String key,
			JsonNode script,
			JsonNode source,
			Optional<JsonNode> metadata,
			Optional<Object> context) {

		try {
			if (!script.getNodeType().equals(JsonNodeType.STRING)) {
				//Todo: capire come bisogna gestire l'eccezione
				throw new JoyceSchemaEngineException("$jsExpr field must contain a string.");
			}

			String scriptWithJsonParsing = this.computeScriptWithJsonParsing(script.asText());
			ScriptContext scriptContext = this.computeScriptContext(source, metadata, context);

			return mapper.convertValue(
					scriptEngine.eval(scriptWithJsonParsing, scriptContext),
					JsonNode.class
			);
		} catch (Exception exception) {
			//Todo: capire come bisogna gestire l'eccezione
			throw new JoyceSchemaEngineException(exception.getMessage());
		}
	}

	private String computeScriptWithJsonParsing(String script) {
		StringBuilder updatedScript = new StringBuilder();

		String regExp = String.format("(%s|%s|%s)", SOURCE_PLACEHOLDER, METADATA_PLACEHOLDER, CONTEXT_PLACEHOLDER);
		Matcher matcher = Pattern.compile(regExp).matcher(script);

		int start = 0;
		while (matcher.find(start)) {
			String placeholder = this.computeJsonParsingPlaceholder(matcher.group(0));
			updatedScript.append(script, start, matcher.start());
			updatedScript.append(placeholder);
			start = matcher.end();
		}
		return updatedScript
				.append(script.substring(start))
				.toString();
	}

	private ScriptContext computeScriptContext(
			JsonNode source,
			Optional<JsonNode> metadata,
			Optional<Object> context) throws JsonProcessingException {

		Map<String, Object> contextBindings = new HashMap<>();
		this.addToContextBindingsIfPresent(SOURCE_PLACEHOLDER, Optional.of(source), contextBindings);
		this.addToContextBindingsIfPresent(METADATA_PLACEHOLDER, metadata, contextBindings);
		this.addToContextBindingsIfPresent(CONTEXT_PLACEHOLDER, context, contextBindings);
		return this.buildNewScriptContext(contextBindings);
	}

	private <T> void addToContextBindingsIfPresent(
			String key,	Optional<T> value,
			Map<String, Object> contextBindings) throws JsonProcessingException {

		if (value.isPresent()) {
			contextBindings.put(key, mapper.writeValueAsString(value.get()));
		}
	}

	private ScriptContext buildNewScriptContext(Map<String, Object> sourceValues) {
		SimpleScriptContext context = new SimpleScriptContext();
		context.setBindings(new SimpleBindings(sourceValues), ScriptContext.ENGINE_SCOPE);
		return context;
	}
}
