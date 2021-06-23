package com.sourcesense.joyce.schemaengine.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.jayway.jsonpath.JsonPath;
import com.sourcesense.joyce.schemaengine.exception.JoyceSchemaEngineException;
import lombok.RequiredArgsConstructor;

import javax.script.AbstractScriptEngine;
import javax.script.ScriptContext;
import javax.script.SimpleBindings;
import javax.script.SimpleScriptContext;
import java.util.Map;
import java.util.Optional;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public abstract class ScriptingTransformerHandler<SE extends AbstractScriptEngine>
		implements SchemaTransformerHandler {

	private final SE scriptEngine;
	private final ObjectMapper mapper;

	protected abstract String computePlaceholderWithJsonParser(String field);

	@Override
	public JsonNode process(
			String key,
			JsonNode value,
			JsonNode source,
			Optional<JsonNode> metadata,
			Optional<Object> context) {

		try {
			if (!value.getNodeType().equals(JsonNodeType.STRING)) {
				//Todo: capire come bisogna gestire l'eccezione
				throw new JoyceSchemaEngineException("Invalid value for $jsExpr handler");
			}

			String script = value.asText();
			Matcher placeholderMatcher = Pattern.compile("\\$\\['(.+?)']").matcher(script);

			String validScript = this.computeValidScript(placeholderMatcher, script);
			Map<String, Object> sourceValues = this.computeValuesFromSource(placeholderMatcher, source);

			ScriptContext scriptContext = this.buildNewScriptContext(sourceValues);
			return mapper.valueToTree(
					scriptEngine.eval(validScript, scriptContext)
			);
		} catch (Exception exception) {
			//Todo: capire come bisogna gestire l'eccezione
			throw new JoyceSchemaEngineException(exception.getMessage());
		}
	}

	private String computeValidScript(Matcher matcher, String script) {
		StringBuilder updatedScript = new StringBuilder();
		int start = 0;
		while (matcher.find(start)) {
			String placeholder = "___" + matcher.group(1) + "___";
			updatedScript.append(script, start, matcher.start());
			updatedScript.append(this.computePlaceholderWithJsonParser(placeholder));
			start = matcher.end();
		}
		return updatedScript
				.append(script.substring(start))
				.toString();
	}

	private Map<String, Object> computeValuesFromSource(Matcher matcher, JsonNode source) {
		return matcher.results()
				.map(MatchResult::group)
				.distinct()
				.collect(Collectors.toMap(
						this::computeValidPlaceholder,
						placeholder -> this.computeValueFromSource(placeholder, source)
				));
	}

	private String computeValidPlaceholder(String placeholder) {
		return placeholder
				.replace("$['", "___")
				.replace("']", "___");
	}

	private String computeValueFromSource(String placeholder, JsonNode source) {
		try {
			return mapper.writeValueAsString(
					JsonPath.read(source, placeholder)
			);
		} catch (Exception exception) {
			//Todo: capire come bisogna gestire l'eccezione
			throw new JoyceSchemaEngineException(exception.getMessage());
		}
	}

	private ScriptContext buildNewScriptContext(Map<String, Object> sourceValues) {
		SimpleScriptContext context = new SimpleScriptContext();
		context.setBindings(new SimpleBindings(sourceValues), ScriptContext.ENGINE_SCOPE);
		return context;
	}
}
