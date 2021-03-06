package com.sourcesense.joyce.schemaengine.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ScriptingTransformerHandlerTest implements TestUtility {

	private ScriptingTransformerHandler scriptingTransformerHandler;

	@BeforeEach
	void init() {
		ObjectMapper mapper = initJsonMapper();
		ApplicationContext context = initApplicationContext(mapper);
		scriptingTransformerHandler = new ScriptingTransformerHandler(
				mapper,
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

		String actual = scriptingTransformerHandler
				.process(key, "string", value, source, Optional.empty(), Optional.empty())
				.asText();

		assertEquals(expected, actual);
	}
}
