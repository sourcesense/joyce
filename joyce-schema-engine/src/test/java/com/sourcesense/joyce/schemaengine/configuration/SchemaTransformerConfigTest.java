package com.sourcesense.joyce.schemaengine.configuration;

import com.fasterxml.jackson.databind.JsonNode;
import com.sourcesense.joyce.schemaengine.annotation.SchemaTransformationHandler;
import com.sourcesense.joyce.schemaengine.exception.InvalidHandlerKeywordException;
import com.sourcesense.joyce.schemaengine.exception.JoyceSchemaEngineException;
import com.sourcesense.joyce.schemaengine.handler.FixedValueTransformerHandler;
import com.sourcesense.joyce.schemaengine.handler.SchemaTransformerHandler;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.context.ApplicationContext;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class SchemaTransformerConfigTest {

	@Test
	void shouldLoadNoHandler() {
		ApplicationContext context = mock(ApplicationContext.class);
		Map<String, Object> customHandlers = Collections.emptyMap();
		when(context.getBeansWithAnnotation(SchemaTransformationHandler.class)).thenReturn(customHandlers);
		SchemaTransformerConfig schemaTransformerConfig = new SchemaTransformerConfig(context);

		Map<String, SchemaTransformerHandler> actual = schemaTransformerConfig.transformerHandlers();
		Map<String, SchemaTransformerHandler> expected = Collections.emptyMap();

		assertTrue(actual.equals(expected));
	}

	@Test
	void shouldLoadCustomHandler() {
		ApplicationContext context = mock(ApplicationContext.class);
		FixedValueTransformerHandler transformerHandler = new FixedValueTransformerHandler();
		Map<String, Object> customHandlers = Map.of(
				"fixedValueTransformerHandler", transformerHandler
		);
		when(context.getBeansWithAnnotation(SchemaTransformationHandler.class)).thenReturn(customHandlers);
		SchemaTransformerConfig schemaTransformerConfig = new SchemaTransformerConfig(context);

		Map<String, SchemaTransformerHandler> actual = schemaTransformerConfig.transformerHandlers();
		Map<String, SchemaTransformerHandler> expected = Map.of("$fixed", transformerHandler);

		assertTrue(actual.equals(expected));
	}

	@Test
	void shouldAddDollarSignIfMissing() {
		ApplicationContext context = mock(ApplicationContext.class);
		TestNoDollarTransformerHandler transformerHandler = new TestNoDollarTransformerHandler();
		Map<String, Object> customHandlers = Map.of(
				"testNoDollarTransformerHandler", transformerHandler
		);
		when(context.getBeansWithAnnotation(SchemaTransformationHandler.class)).thenReturn(customHandlers);
		SchemaTransformerConfig schemaTransformerConfig = new SchemaTransformerConfig(context);

		Map<String, SchemaTransformerHandler> actual = schemaTransformerConfig.transformerHandlers();
		Map<String, SchemaTransformerHandler> expected = Map.of("$test", transformerHandler);

		assertTrue(actual.equals(expected));
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
		public JsonNode process(String key, String type, JsonNode value, JsonNode source, Optional<JsonNode> metadata, Optional<Object> context) {
			return null;
		}
	}

	@SchemaTransformationHandler(keyword = "test")
	private static class TestNoDollarTransformerHandler implements SchemaTransformerHandler {

		@Override
		public JsonNode process(String key, String type, JsonNode value, JsonNode source, Optional<JsonNode> metadata, Optional<Object> context) {
			return null;
		}
	}
}
