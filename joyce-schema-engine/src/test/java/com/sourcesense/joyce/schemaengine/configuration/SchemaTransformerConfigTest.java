package com.sourcesense.joyce.schemaengine.configuration;

import com.fasterxml.jackson.databind.JsonNode;
import com.sourcesense.joyce.schemaengine.annotation.SchemaTransformationHandler;
import com.sourcesense.joyce.schemaengine.exception.InvalidHandlerKeywordException;
import com.sourcesense.joyce.schemaengine.exception.JoyceSchemaEngineException;
import com.sourcesense.joyce.schemaengine.handler.JsonPathTransformerHandler;
import com.sourcesense.joyce.schemaengine.handler.SchemaTransformerHandler;
import com.sourcesense.joyce.schemaengine.model.SchemaEngineContext;
import com.sourcesense.joyce.schemaengine.test.TestUtility;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.context.ApplicationContext;

import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class SchemaTransformerConfigTest implements TestUtility {

	@Test
	void shouldLoadNoHandler() {
		ApplicationContext context = mock(ApplicationContext.class);
		Map<String, Object> customHandlers = Collections.emptyMap();
		when(context.getBeansWithAnnotation(SchemaTransformationHandler.class)).thenReturn(customHandlers);
		SchemaTransformerConfig schemaTransformerConfig = new SchemaTransformerConfig(context);

		Map<String, SchemaTransformerHandler> actual = schemaTransformerConfig.transformerHandlers();
		Map<String, SchemaTransformerHandler> expected = Collections.emptyMap();

		assertEquals(actual, expected);
	}

	@Test
	void shouldLoadCustomHandler() {
		ApplicationContext context = mock(ApplicationContext.class);
		JsonPathTransformerHandler transformerHandler = new JsonPathTransformerHandler(jsonMapper);
		Map<String, Object> customHandlers = Map.of(
				"jsonPathTransformerHandler", transformerHandler
		);
		when(context.getBeansWithAnnotation(SchemaTransformationHandler.class)).thenReturn(customHandlers);
		SchemaTransformerConfig schemaTransformerConfig = new SchemaTransformerConfig(context);

		Map<String, SchemaTransformerHandler> actual = schemaTransformerConfig.transformerHandlers();
		Map<String, SchemaTransformerHandler> expected = Map.of("extract", transformerHandler);

		assertEquals(actual, expected);
	}

	@Test
	void shouldNotLoadHandlerNotImplementingSchemaTransformerHandler() {
		ApplicationContext context = mock(ApplicationContext.class);
		TestNotImplementingSchemaTransformerHandler transformerHandler = new TestNotImplementingSchemaTransformerHandler();
		Map<String, Object> customHandlers = Map.of(
				"testNotImplementingSchemaTransformerHandler", transformerHandler
		);
		when(context.getBeansWithAnnotation(SchemaTransformationHandler.class)).thenReturn(customHandlers);
		SchemaTransformerConfig schemaTransformerConfig = new SchemaTransformerConfig(context);

		assertThrows(
				JoyceSchemaEngineException.class,
				schemaTransformerConfig::transformerHandlers
		);
	}

	@Test
	void shouldNotLoadEmptyKeywordTransformerHandler() {
		ApplicationContext context = mock(ApplicationContext.class);
		TestEmptyKeywordTransformerHandler transformerHandler = new TestEmptyKeywordTransformerHandler();
		Map<String, Object> customHandlers = Map.of(
				"testEmptyKeywordTransformerHandler", transformerHandler
		);
		when(context.getBeansWithAnnotation(SchemaTransformationHandler.class)).thenReturn(customHandlers);
		SchemaTransformerConfig schemaTransformerConfig = new SchemaTransformerConfig(context);

		assertThrows(
				InvalidHandlerKeywordException.class,
				schemaTransformerConfig::transformerHandlers
		);
	}

	@SchemaTransformationHandler(keyword = "notImplementing")
	private static class TestNotImplementingSchemaTransformerHandler {
	}

	@SchemaTransformationHandler
	private static class TestEmptyKeywordTransformerHandler implements SchemaTransformerHandler {

		@Override
		public JsonNode process(String key, String type, JsonNode args, SchemaEngineContext context) {
			return null;
		}
	}
}
