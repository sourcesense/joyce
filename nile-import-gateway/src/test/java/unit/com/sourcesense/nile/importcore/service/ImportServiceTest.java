package unit.com.sourcesense.nile.importcore.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sourcesense.nile.core.dto.Schema;
import com.sourcesense.nile.core.exception.InvalidMetadataException;
import com.sourcesense.nile.core.model.NileURI;
import com.sourcesense.nile.core.service.MainlogProducer;
import com.sourcesense.nile.core.service.SchemaService;
import com.sourcesense.nile.importcore.service.ImportService;
import com.sourcesense.nile.schemaengine.exceptions.InvalidSchemaException;
import com.sourcesense.nile.schemaengine.exceptions.SchemaIsNotValidException;
import com.sourcesense.nile.schemaengine.service.SchemaEngine;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.mockito.ArgumentMatchers.any;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class ImportServiceTest {
	private static final String TEST_SCHEMA_JSON_USER = "test-schema-user.json";
	private static final String TEST_SCHEMA_JSON_ENANCHED_USER = "test-schema-enhanced-user.json";
	private static final String TEST_USER_JSON = "test-user.json";

	// Mocked components
	@Mock
	private SchemaEngine schemaEngine;
	@Mock
	private SchemaService schemaService;
	@Mock
	private MainlogProducer mainlogProducer;

	private final ObjectMapper objectMapper = new ObjectMapper();

	// Subject under test
	private ImportService importService;

	// CONSTANTS
	private static final String MESSAGE_KEY = "nile://raw/other/user/1";

	@BeforeEach
	void init() {
		importService = new ImportService(objectMapper, schemaEngine, schemaService, mainlogProducer);
	}

	@Test
	void shouldProcessImportWhenTheInputDataIsCorrect() throws IOException {

		// mocking and stubbing data for test execution
		Schema schema = computeSchema(TEST_SCHEMA_JSON_USER);
		NileURI rawURI = NileURI.createURI(MESSAGE_KEY).orElseThrow();
		Mockito.when(schemaEngine.process(any(), any(), any()))
				.thenReturn(objectMapper.valueToTree(Map.of("code", "1337")));

		// Subject under test
		boolean result = importService.processImport(rawURI, computeDocument(), schema);

		// Asserts
		Assertions.assertTrue(result);
	}

	@Test
	void ShouldThrowInvalidMetadataExceptionIfSchemaHasNotMetadata() throws IOException {
		// mocking and stubbing data for test execution
		Schema schema = new Schema();

		// asserts
		Assertions.assertThrows(InvalidMetadataException.class,
				() -> importService.processImport(null, null, schema));
	}

	@Test
	void ShouldThrowSchemaIsNotValidExceptionIfSchemaIsNotValidated() throws IOException {
		// mocking and stubbing data for test execution
		Schema schema = computeSchema(TEST_SCHEMA_JSON_USER);
		Mockito.when( schemaEngine.process(any(),any(),any())).thenThrow(SchemaIsNotValidException.class);

		// asserts
		Assertions
				.assertThrows(SchemaIsNotValidException.class, () -> importService.processImport(null, null, schema));
	}
	@Test
	void ShouldThrowInvalidSchemaExceptionIfSchemaHasParentButParentSchemaIsNotAlreadyPresentInDb() throws IOException {
		// mocking and stubbing data for test execution
		Schema schema = computeSchema(TEST_SCHEMA_JSON_ENANCHED_USER);

		// asserts
		Assertions
				.assertThrows(InvalidSchemaException.class, () -> importService.processImport(null, null, schema));
	}
	@Test
	void ShouldProcessImportIfSchemaHasParentAndParentSchemaIsAlreadyPresentInDb() throws IOException {
		// mocking and stubbing data for test execution
		Schema schema = computeSchema(TEST_SCHEMA_JSON_ENANCHED_USER);
		NileURI rawURI = NileURI.createURI(MESSAGE_KEY).orElseThrow();
		Mockito.when(schemaEngine.process(any(), any(), any()))
				.thenReturn(objectMapper.valueToTree(Map.of("code", "1337")));
		Mockito.when(schemaService.get(any())).thenReturn(Optional.of(computeSchema(TEST_SCHEMA_JSON_USER)));

		// asserts
		Assertions
				.assertTrue(importService.processImport(null, null, schema));
	}



	/* UTILITY METHODS */
	private Schema computeSchema(String jsonSchemaName) throws IOException {
		return objectMapper.readValue(computeJsonFromResource(jsonSchemaName),
				Schema.class);
	}

	private JsonNode computeDocument() throws IOException {
		return objectMapper.readTree(computeJsonFromResource(TEST_USER_JSON));
	}

	private InputStream computeJsonFromResource(String jsonFileName) {
		return this.getClass().getClassLoader().getResourceAsStream(jsonFileName);
	}
}
