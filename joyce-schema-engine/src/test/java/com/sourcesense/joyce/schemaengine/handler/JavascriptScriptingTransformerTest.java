package com.sourcesense.joyce.schemaengine.handler;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.oracle.truffle.js.scriptengine.GraalJSScriptEngine;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.HostAccess;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class JavascriptScriptingTransformerTest {

	private ObjectMapper mapper;
	private JavascriptScriptingTransformerHandler javascriptScriptingTransformerHandler;

	@BeforeEach
	void init() {
		mapper = new ObjectMapper();
		javascriptScriptingTransformerHandler =
				new JavascriptScriptingTransformerHandler(
						this.initGraalJSScriptEngine(),
						this.initMapper()
				);
	}

	@Test
	void shouldProcessForNoValue() throws IOException, URISyntaxException {
		String key = "noValue";
		JsonNode source = this.getResourceAsNode("source/31.json");
		JsonNode script = mapper.convertValue(
				"'noValue'.substring(0,1)",
				TextNode.class
		);

		String expected = "n";
		String actual = javascriptScriptingTransformerHandler
				.process(key, script, source, Optional.empty(), Optional.empty())
				.textValue();

		assertEquals(expected, actual);
	}

	@Test
	void shouldProcessForSimpleField() throws IOException, URISyntaxException {
		String key = "lowercaseSimpleField";
		JsonNode source = this.getResourceAsNode("source/31.json");
		JsonNode script = mapper.convertValue(
				"__source.simpleField.toLowerCase()",
				TextNode.class
		);

		String expected = "simplefieldvalue";
		String actual = javascriptScriptingTransformerHandler
				.process(key, script, source, Optional.empty(), Optional.empty())
				.textValue();

		assertEquals(expected, actual);
	}

	@Test
	void shouldProcessForStringArray() throws IOException, URISyntaxException {
		String key = "reducedStringArray";
		JsonNode source = this.getResourceAsNode("source/31.json");
		JsonNode script = mapper.convertValue(
				"__source.stringArray.reduce((a,b) => `${a}-${b}`, 'start')",
				TextNode.class
		);

		String expected = "start-stringArrayValue1-stringArrayValue2-stringArrayValueN";
		String actual = javascriptScriptingTransformerHandler
				.process(key, script, source, Optional.empty(), Optional.empty())
				.textValue();

		assertEquals(expected, actual);
	}

	@Test
	void shouldProcessForComplexArray() throws IOException, URISyntaxException {
		String key = "uppercaseComplexArrayField";
		JsonNode source = this.getResourceAsNode("source/31.json");
		JsonNode script = mapper.convertValue(
				"__source.complexArray[0]['complexArrayField'].toUpperCase()",
				TextNode.class
		);

		String expected = "COMPLEXARRAYVALUE1";
		String actual = javascriptScriptingTransformerHandler
				.process(key, script, source, Optional.empty(), Optional.empty())
				.textValue();

		assertEquals(expected, actual);
	}


//	@Test
//	void shouldProcessForObject() throws IOException, URISyntaxException {
//		String key = "mapComplexArrayToValues";
//		JsonNode source = this.getResourceAsNode("source/31.json");
//		JsonNode script = mapper.convertValue(
//				"__source.object.nestedObject.nestedObjectComplexArray.map(e => e.nestedObjectComplexArrayField)",
//				TextNode.class
//		);
//
//		Object[] expected = {
//				"nestedObjectComplexArrayValue1",
//				"nestedObjectComplexArrayValue2",
//				"nestedObjectComplexArrayValueN"
//		};
//		Object[] actual = mapper.convertValue(
//				javascriptScriptingTransformerHandler.process(key, script, source, Optional.empty(), Optional.empty()),
//				Object[].class
//		);
//		assertArrayEquals(expected, actual);
//	}

	private ObjectMapper initMapper() {
		return new ObjectMapper()
				.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
				.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
	}

	private GraalJSScriptEngine initGraalJSScriptEngine() {
		return GraalJSScriptEngine.create(
				null,
				Context.newBuilder("js")
						.allowHostAccess(HostAccess.ALL)
						.allowHostClassLookup(s -> true)
						.option("js.ecmascript-version", "2021")
		);
	}

	private JsonNode getResourceAsNode(String path) throws IOException, URISyntaxException {
		URL res = this.getClass().getClassLoader().getResource(path);
		return mapper.readValue(
				Files.readAllBytes(Path.of(res.toURI())),
				JsonNode.class
		);
	}
}
