package com.sourcesense.joyce.schemaengine.templating.mustache.resolver;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.*;
import com.samskivert.mustache.Mustache;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@RequiredArgsConstructor
public class MustacheTemplateResolver {

	private final Mustache.Compiler mustacheCompiler;
	private final Map<String, Object> mustacheContext;

	public JsonNode resolve(JsonNode input) {
		return this.resolveJsonNode(input.deepCopy());
	}

	public String resolve(String template) {
		return mustacheCompiler
				.compile(template)
				.execute(mustacheContext);
	}

	private JsonNode resolveJsonNode(JsonNode input) {
		if (JsonNodeType.STRING.equals(input.getNodeType())) {
			return JsonNodeFactory.instance.textNode(
					this.resolve(input.textValue())
			);
		} else if (input.isObject()) {
			this.resolveObjectNode((ObjectNode) input);

		} else if (input.isArray()) {
			this.resolveArrayNode((ArrayNode) input);
		}
		return input;
	}

	private void resolveObjectNode(ObjectNode input) {
		input.fields().forEachRemaining(entry -> {
			JsonNode output = this.resolve(entry.getValue());
			if (entry.getValue() != output) {
				input.replace(entry.getKey(), output);
			}
		});
	}

	private void resolveArrayNode(ArrayNode input) {
		for (int i = 0; i < input.size(); i++) {
			JsonNode output = this.resolve(input.path(i));
			if (input.path(i) != output) {
				input.set(i, output);
			}
		}
	}
}
