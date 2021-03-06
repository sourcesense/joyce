package com.sourcesense.joyce.schemaengine.service.scripting;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sourcesense.joyce.schemaengine.exception.JoyceSchemaEngineException;
import com.sourcesense.joyce.schemaengine.model.handler.ScriptHandlerData;
import com.sourcesense.joyce.schemaengine.test.TestUtility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class JavaScriptScriptingServiceTest implements TestUtility {

	private ObjectMapper mapper;
	private JavaScriptScriptingService javaScriptScriptingService;

	@BeforeEach
	void init() {
		mapper = this.initJsonMapper();
		javaScriptScriptingService = new JavaScriptScriptingService(
				mapper,
				this.initGraalJSScriptEngine()
		);
	}

	@Test
	void shouldNotProcessVoidScript() throws IOException, URISyntaxException {
		String key = "void";
		JsonNode source = this.getResourceAsNode("source/31.json");
		ScriptHandlerData scriptHandlerData = mapper.convertValue(
				this.getResourceAsNode("script/javascript/31.json"),
				ScriptHandlerData.class
		);

		assertThrows(
				JoyceSchemaEngineException.class,
				() -> javaScriptScriptingService.eval(
						key, scriptHandlerData, source, Optional.empty(), Optional.empty()
				)
		);
	}

	@Test
	void shouldProcessForNoValue() throws IOException, URISyntaxException {
		String key = "noValue";
		JsonNode source = this.getResourceAsNode("source/31.json");
		ScriptHandlerData scriptHandlerData = mapper.convertValue(
				this.getResourceAsNode("script/javascript/32.json"),
				ScriptHandlerData.class
		);

		String expected = "n";
		String actual = javaScriptScriptingService
				.eval(key, scriptHandlerData, source, Optional.empty(), Optional.empty())
				.textValue();

		assertEquals(expected, actual);
	}

	@Test
	void shouldProcessForSimpleField() throws IOException, URISyntaxException {
		String key = "lowercaseSimpleField";
		JsonNode source = this.getResourceAsNode("source/31.json");
		ScriptHandlerData scriptHandlerData = mapper.convertValue(
				this.getResourceAsNode("script/javascript/33.json"),
				ScriptHandlerData.class
		);

		String expected = "simplefieldvalue";
		String actual = javaScriptScriptingService
				.eval(key, scriptHandlerData, source, Optional.empty(), Optional.empty())
				.textValue();

		assertEquals(expected, actual);
	}

	@Test
	void shouldProcessForStringArray() throws IOException, URISyntaxException {
		String key = "reducedStringArray";
		JsonNode source = this.getResourceAsNode("source/31.json");
		ScriptHandlerData scriptHandlerData = mapper.convertValue(
				this.getResourceAsNode("script/javascript/34.json"),
				ScriptHandlerData.class
		);

		String expected = "start-stringArrayValue1-stringArrayValue2-stringArrayValueN";
		String actual = javaScriptScriptingService
				.eval(key, scriptHandlerData, source, Optional.empty(), Optional.empty())
				.textValue();

		assertEquals(expected, actual);
	}

	@Test
	void shouldProcessForComplexArray() throws IOException, URISyntaxException {
		String key = "uppercaseComplexArrayField";
		JsonNode source = this.getResourceAsNode("source/31.json");
		ScriptHandlerData scriptHandlerData = mapper.convertValue(
				this.getResourceAsNode("script/javascript/35.json"),
				ScriptHandlerData.class
		);

		String expected = "COMPLEXARRAYVALUE1";
		String actual = javaScriptScriptingService
				.eval(key, scriptHandlerData, source, Optional.empty(), Optional.empty())
				.textValue();

		assertEquals(expected, actual);
	}


	@Test
	void shouldProcessForObject() throws IOException, URISyntaxException {
		String key = "mapComplexArrayToValues";
		JsonNode source = this.getResourceAsNode("source/31.json");
		ScriptHandlerData scriptHandlerData = mapper.convertValue(
				this.getResourceAsNode("script/javascript/36.json"),
				ScriptHandlerData.class
		);

		Object[] expected = {
				"nestedObjectComplexArrayValue1",
				"nestedObjectComplexArrayValue2",
				"nestedObjectComplexArrayValueN"
		};
		Object[] actual = mapper.convertValue(
				javaScriptScriptingService.eval(key, scriptHandlerData, source, Optional.empty(), Optional.empty()),
				Object[].class
		);
		assertArrayEquals(expected, actual);
	}

	@Test
	void shouldProcessMultilineScript() throws IOException, URISyntaxException {
		String key = "multilineScript";
		JsonNode source = this.getResourceAsNode("source/31.json");
		ScriptHandlerData scriptHandlerData = mapper.convertValue(
				this.getResourceAsNode("script/javascript/38.json"),
				ScriptHandlerData.class
		);

		String expected = "multiline";
		String actual = javaScriptScriptingService
				.eval(key, scriptHandlerData, source, Optional.empty(), Optional.empty())
				.textValue();

		assertEquals(expected, actual);
	}
}
