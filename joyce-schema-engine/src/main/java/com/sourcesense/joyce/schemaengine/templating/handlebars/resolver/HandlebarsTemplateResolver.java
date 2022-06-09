package com.sourcesense.joyce.schemaengine.templating.handlebars.resolver;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.github.jknack.handlebars.Handlebars;
import com.sourcesense.joyce.schemaengine.exception.JoyceSchemaEngineException;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
public class HandlebarsTemplateResolver {

	private final ObjectMapper jsonMapper;
	private final Handlebars handlebars;


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
		try {
			return handlebars.compileInline(template).apply(this.computeContext(scope));

		} catch (Exception exception) {
			throw new JoyceSchemaEngineException(String.format(
					"Unable to resolve handlebars template, error message is '%s'", exception.getMessage()
			));
		}
	}

	private JsonNode resolveJsonNode(JsonNode input, Object scope) {
		if (JsonNodeType.STRING.equals(input.getNodeType())) {
			return new TextNode(
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

	private Map<String, Object> computeContext(Object scope) {
		return Optional.ofNullable(scope)
				.map(object -> jsonMapper.convertValue(scope, new TypeReference<Map<String, Object>>() {}))
				.map(Object.class::cast)
				.map(convertedScope -> Map.of("ctx", convertedScope))
				.orElseGet(HashMap::new);
	}
}
