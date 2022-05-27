package com.sourcesense.joyce.schemaengine.service.scripting;

import com.fasterxml.jackson.databind.JsonNode;
import com.sourcesense.joyce.schemaengine.exception.JoyceSchemaEngineException;
import com.sourcesense.joyce.schemaengine.model.dto.SchemaEngineContext;
import com.sourcesense.joyce.schemaengine.model.dto.handler.ScriptHandlerArgs;
import com.sourcesense.joyce.schemaengine.test.TestUtility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.net.URISyntaxException;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class JavaScriptScriptingServiceTest implements TestUtility {

	private JavaScriptScriptingService javaScriptScriptingService;

	@BeforeEach
	void init() {
		javaScriptScriptingService = new JavaScriptScriptingService(
				jsonMapper,
				this.initGraalJSScriptEngine()
		);
	}

	@Test
	void shouldNotProcessVoidScript() throws IOException, URISyntaxException {
		String key = "void";
		JsonNode source = this.getResourceAsNode("source/31.json");
		ScriptHandlerArgs scriptHandlerArgs = jsonMapper.convertValue(
				this.getResourceAsNode("script/javascript/31.json"),
				ScriptHandlerArgs.class
		);

		assertThrows(
				JoyceSchemaEngineException.class,
				() -> javaScriptScriptingService.eval(key, scriptHandlerArgs, this.buildContext(source))
		);
	}

	@Test
	void shouldProcessForNoValue() throws IOException, URISyntaxException {
		String key = "noValue";
		JsonNode source = this.getResourceAsNode("source/31.json");
		ScriptHandlerArgs scriptHandlerArgs = jsonMapper.convertValue(
				this.getResourceAsNode("script/javascript/32.json"),
				ScriptHandlerArgs.class
		);

		String expected = "n";
		String actual = javaScriptScriptingService
				.eval(key, scriptHandlerArgs, this.buildContext(source))
				.textValue();

		assertEquals(expected, actual);
	}

	@Test
	void shouldProcessForSimpleField() throws IOException, URISyntaxException {
		String key = "lowercaseSimpleField";
		JsonNode source = this.getResourceAsNode("source/31.json");
		ScriptHandlerArgs scriptHandlerArgs = jsonMapper.convertValue(
				this.getResourceAsNode("script/javascript/33.json"),
				ScriptHandlerArgs.class
		);

		String expected = "simplefieldvalue";
		String actual = javaScriptScriptingService
				.eval(key, scriptHandlerArgs, this.buildContext(source))
				.textValue();

		assertEquals(expected, actual);
	}

	@Test
	void shouldProcessForStringArray() throws IOException, URISyntaxException {
		String key = "reducedStringArray";
		JsonNode source = this.getResourceAsNode("source/31.json");
		ScriptHandlerArgs scriptHandlerArgs = jsonMapper.convertValue(
				this.getResourceAsNode("script/javascript/34.json"),
				ScriptHandlerArgs.class
		);

		String expected = "start-stringArrayValue1-stringArrayValue2-stringArrayValueN";
		String actual = javaScriptScriptingService
				.eval(key, scriptHandlerArgs, this.buildContext(source))
				.textValue();

		assertEquals(expected, actual);
	}

	@Test
	void shouldProcessForComplexArray() throws IOException, URISyntaxException {
		String key = "uppercaseComplexArrayField";
		JsonNode source = this.getResourceAsNode("source/31.json");
		ScriptHandlerArgs scriptHandlerArgs = jsonMapper.convertValue(
				this.getResourceAsNode("script/javascript/35.json"),
				ScriptHandlerArgs.class
		);

		String expected = "COMPLEXARRAYVALUE1";
		String actual = javaScriptScriptingService
				.eval(key, scriptHandlerArgs, this.buildContext(source))
				.textValue();

		assertEquals(expected, actual);
	}


	@Test
	void shouldProcessForObject() throws IOException, URISyntaxException {
		String key = "mapComplexArrayToValues";
		JsonNode source = this.getResourceAsNode("source/31.json");
		ScriptHandlerArgs scriptHandlerArgs = jsonMapper.convertValue(
				this.getResourceAsNode("script/javascript/36.json"),
				ScriptHandlerArgs.class
		);

		Object[] expected = {
				"nestedObjectComplexArrayValue1",
				"nestedObjectComplexArrayValue2",
				"nestedObjectComplexArrayValueN"
		};
		Object[] actual = jsonMapper.convertValue(
				javaScriptScriptingService.eval(key, scriptHandlerArgs, this.buildContext(source)),
				Object[].class
		);
		assertArrayEquals(expected, actual);
	}

	@Test
	void shouldProcessMultilineScript() throws IOException, URISyntaxException {
		String key = "multilineScript";
		JsonNode source = this.getResourceAsNode("source/31.json");
		ScriptHandlerArgs scriptHandlerArgs = jsonMapper.convertValue(
				this.getResourceAsNode("script/javascript/38.json"),
				ScriptHandlerArgs.class
		);

		String expected = "multiline";
		String actual = javaScriptScriptingService
				.eval(key, scriptHandlerArgs, this.buildContext(source))
				.textValue();

		assertEquals(expected, actual);
	}

	private SchemaEngineContext buildContext(JsonNode source) {
		return SchemaEngineContext.builder()
				.src(source)
				.out(source)
				.build();
	}
}
