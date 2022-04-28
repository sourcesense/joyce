package com.sourcesense.joyce.schemaengine.templating.mustache.resolver;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.*;
import com.samskivert.mustache.Mustache;
import com.samskivert.mustache.Template;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MustacheTemplateResolverTest {

	private final static String TEST_STRING_INPUT = "Test - {{#test}}some text{{/test}} - Test - {{#test}}more text{{/test}} - Test";
	private final static String TEST_STRING_OUTPUT = "Test - SOME TEXT - Test - MORE TEXT - Test";

	private final static TextNode TEST_NODE_INPUT = JsonNodeFactory.instance.textNode(TEST_STRING_INPUT);
	private final static TextNode TEST_NODE_OUTPUT = JsonNodeFactory.instance.textNode(TEST_STRING_OUTPUT);

	private MustacheTemplateResolver mustacheTemplateResolver;

	@BeforeEach
	public void init() {
		mustacheTemplateResolver = new MustacheTemplateResolver(
				Mustache.compiler(),
				this.initMustacheContext()
		);
	}

	@Test
	public void shouldResolveString() {
		assertEquals(
				TEST_STRING_OUTPUT,
				mustacheTemplateResolver.resolve(TEST_STRING_INPUT)
		);
	}

	@Test
	public void shouldDoNothingOnNullNode() {
		NullNode nullNode = JsonNodeFactory.instance.nullNode();
		assertEquals(
				nullNode,
				mustacheTemplateResolver.resolve(nullNode));
	}

	@Test
	public void shouldDoNothingOnMissingNode() {
		JsonNode missingNode = JsonNodeFactory.instance.missingNode();
		assertEquals(
				missingNode,
				mustacheTemplateResolver.resolve(missingNode));
	}

	@Test
	public void shouldDoNothingOnNumberNode() {
		NumericNode numberNode = JsonNodeFactory.instance.numberNode(1);
		assertEquals(
				numberNode,
				mustacheTemplateResolver.resolve(numberNode)
		);
	}

	@Test
	public void shouldDoNothingOnTextNodeWithNoTemplate() {
		TextNode textNode = JsonNodeFactory.instance.textNode("test");
		assertEquals(
				textNode,
				mustacheTemplateResolver.resolve(textNode)
		);
	}

	@Test
	public void shouldResolveTextNodeWithTemplate() {
		assertEquals(
				TEST_NODE_OUTPUT,
				mustacheTemplateResolver.resolve(TEST_NODE_INPUT)
		);
	}

	@Test
	public void shouldResolveTextNodeWithTemplateInsideObjectNode() {
		JsonNode input = JsonNodeFactory.instance.objectNode().set("test", TEST_NODE_INPUT);
		assertEquals(
				JsonNodeFactory.instance.objectNode().set("test", TEST_NODE_OUTPUT),
				mustacheTemplateResolver.resolve(input)
		);
	}

	@Test
	public void shouldResolveTextNodeWithTemplateInsideArrayNode() {
		ArrayNode input = JsonNodeFactory.instance.arrayNode().add(TEST_NODE_INPUT);
		assertEquals(
				JsonNodeFactory.instance.arrayNode().add(TEST_NODE_OUTPUT),
				mustacheTemplateResolver.resolve(input)
		);
	}

	private Map<String, Object> initMustacheContext() {
		return Map.of(
			"test", new TestLambda()
		);
	}

	@RequiredArgsConstructor
	private static class TestLambda implements Mustache.Lambda {

		@Override
		public void execute(Template.Fragment fragment, Writer writer) throws IOException {
			writer.append(fragment.execute().toUpperCase());
		}
	}
}
