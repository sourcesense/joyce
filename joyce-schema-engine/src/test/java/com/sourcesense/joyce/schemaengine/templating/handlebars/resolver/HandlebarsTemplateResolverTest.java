package com.sourcesense.joyce.schemaengine.templating.handlebars.resolver;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.*;
import com.sourcesense.joyce.schemaengine.test.SchemaEngineJoyceTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class HandlebarsTemplateResolverTest extends SchemaEngineJoyceTest {

	private final static String TEST_STRING_INPUT = "Test - {{#test}}some text{{/test}} - Test - {{#test}}more text{{/test}} - Test";
	private final static String TEST_STRING_OUTPUT = "Test - SOME TEXT - Test - MORE TEXT - Test";

	private final static TextNode TEST_NODE_INPUT = new TextNode(TEST_STRING_INPUT);
	private final static TextNode TEST_NODE_OUTPUT = new TextNode(TEST_STRING_OUTPUT);

	private HandlebarsTemplateResolver handlebarsTemplateResolver;

	@BeforeEach
	public void init() {
		handlebarsTemplateResolver = new HandlebarsTemplateResolver(jsonMapper,	this.computeHandlebars());
	}

	@Test
	public void shouldResolveString() {
		assertEquals(
				TEST_STRING_OUTPUT,
				handlebarsTemplateResolver.resolve(TEST_STRING_INPUT)
		);
	}

	@Test
	public void shouldDoNothingOnNullNode() {
		NullNode nullNode = JsonNodeFactory.instance.nullNode();
		assertEquals(
				nullNode,
				handlebarsTemplateResolver.resolve(nullNode));
	}

	@Test
	public void shouldDoNothingOnMissingNode() {
		JsonNode missingNode = JsonNodeFactory.instance.missingNode();
		assertEquals(
				missingNode,
				handlebarsTemplateResolver.resolve(missingNode));
	}

	@Test
	public void shouldDoNothingOnNumberNode() {
		NumericNode numberNode = JsonNodeFactory.instance.numberNode(1);
		assertEquals(
				numberNode,
				handlebarsTemplateResolver.resolve(numberNode)
		);
	}

	@Test
	public void shouldDoNothingOnTextNodeWithNoTemplate() {
		TextNode textNode = JsonNodeFactory.instance.textNode("test");
		assertEquals(
				textNode,
				handlebarsTemplateResolver.resolve(textNode)
		);
	}

	@Test
	public void shouldResolveTextNodeWithTemplate() {
		assertEquals(
				TEST_NODE_OUTPUT,
				handlebarsTemplateResolver.resolve(TEST_NODE_INPUT)
		);
	}

	@Test
	public void shouldResolveTextNodeWithTemplateInsideObjectNode() {
		JsonNode input = JsonNodeFactory.instance.objectNode().set("test", TEST_NODE_INPUT);
		assertEquals(
				JsonNodeFactory.instance.objectNode().set("test", TEST_NODE_OUTPUT),
				handlebarsTemplateResolver.resolve(input)
		);
	}

	@Test
	public void shouldResolveTextNodeWithTemplateInsideArrayNode() {
		ArrayNode input = JsonNodeFactory.instance.arrayNode().add(TEST_NODE_INPUT);
		assertEquals(
				JsonNodeFactory.instance.arrayNode().add(TEST_NODE_OUTPUT),
				handlebarsTemplateResolver.resolve(input)
		);
	}
}
