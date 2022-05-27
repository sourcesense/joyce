package com.sourcesense.joyce.schemaengine.templating.mustache.resolver;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.samskivert.mustache.Mustache;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@RequiredArgsConstructor
public class MustacheTemplateResolver {

	private final ObjectMapper jsonMapper;
	private final Mustache.Compiler mustacheCompiler;
	private final Map<String, Mustache.Lambda> mustacheLambdas;


	public JsonNode resolve(JsonNode input) {
		return this.resolve(input, null);
	}

	public JsonNode resolve(JsonNode input, Object scope) {
		return this.resolveJsonNode(input.deepCopy(), scope);
	}

	public String resolve(String template) {
		return this.resolve(template, null);
	}

	public String resolve(String template, Object scope) {
		return mustacheCompiler
				.compile(template)
				.execute(Objects.nonNull(scope) ? this.computeEnrichedScope(scope) : mustacheLambdas);
	}


	private JsonNode resolveJsonNode(JsonNode input, Object scope) {
		if (JsonNodeType.STRING.equals(input.getNodeType())) {
			return JsonNodeFactory.instance.textNode(
					this.resolve(input.textValue(), scope)
			);
		} else if (input.isObject()) {
			this.resolveObjectNode((ObjectNode) input, scope);

		} else if (input.isArray()) {
			this.resolveArrayNode((ArrayNode) input, scope);
		}
		return input;
	}

	private void resolveObjectNode(ObjectNode input, Object scope) {
		input.fields().forEachRemaining(entry -> {
			JsonNode output = this.resolve(entry.getValue(), scope);
			if (entry.getValue() != output) {
				input.replace(entry.getKey(), output);
			}
		});
	}

	private void resolveArrayNode(ArrayNode input, Object scope) {
		for (int i = 0; i < input.size(); i++) {
			JsonNode output = this.resolve(input.path(i), scope);
			if (input.path(i) != output) {
				input.set(i, output);
			}
		}
	}

	private Map<String, Object> computeEnrichedScope(Object scope) {
		Map<String, Object> convertedScope = jsonMapper.convertValue(scope, new TypeReference<>() {});
		Map<String, Object> enrichedScope = new HashMap<>();
		enrichedScope.put("ctx", convertedScope);
		enrichedScope.putAll(mustacheLambdas);
		return enrichedScope;
	}
}
