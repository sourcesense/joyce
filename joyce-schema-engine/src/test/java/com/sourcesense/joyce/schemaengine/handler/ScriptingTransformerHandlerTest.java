package com.sourcesense.joyce.schemaengine.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.sourcesense.joyce.schemaengine.model.SchemaEngineContext;
import com.sourcesense.joyce.schemaengine.test.TestUtility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.context.ApplicationContext;

import java.io.IOException;
import java.net.URISyntaxException;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ScriptingTransformerHandlerTest implements TestUtility {

	private ScriptingTransformerHandler scriptingTransformerHandler;

	@BeforeEach
	void init() {
		ApplicationContext context = initApplicationContext(jsonMapper);
		scriptingTransformerHandler = new ScriptingTransformerHandler(
				jsonMapper,
				context
		);
	}

	@Test
	void shouldProcessOneLineJavascript() throws IOException, URISyntaxException {
		this.shouldProcessScript(
				"oneLineJavascript",
				"script/javascript/37.json",
				"one line"
				);
	}

	@Test
	void shouldProcessMultiLineJavascript() throws IOException, URISyntaxException {
		this.shouldProcessScript(
				"multiLineJavascript",
				"script/javascript/38.json",
				"multiline"
		);
	}

	@Test
	void shouldProcessOneLineGroovy() throws IOException, URISyntaxException {
		this.shouldProcessScript(
				"oneLineGroovy",
				"script/groovy/37.json",
				"one line"
		);
	}

	@Test
	void shouldProcessMultiLineGroovy() throws IOException, URISyntaxException {
		this.shouldProcessScript(
				"multilineGroovy",
				"script/groovy/38.json",
				"multiline"
		);
	}

	private void shouldProcessScript(
			String key,
			String valuePath,
			String expected) throws IOException, URISyntaxException {

		JsonNode source = this.getResourceAsNode("source/31.json");
		JsonNode value = this.getResourceAsNode(valuePath);

		SchemaEngineContext context = SchemaEngineContext.builder()
				.out(source)
				.build();

		String actual = scriptingTransformerHandler
				.process(key, "string", value, context)
				.asText();

		assertEquals(expected, actual);
	}
}
