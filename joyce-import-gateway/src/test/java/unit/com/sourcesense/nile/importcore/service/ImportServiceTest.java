/*
 * Copyright 2021 Sourcesense Spa
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package unit.com.sourcesense.joyce.importcore.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sourcesense.joyce.core.dto.Schema;
import com.sourcesense.joyce.core.enumeration.KafkaCustomHeaders;
import com.sourcesense.joyce.core.exception.InvalidMetadataException;
import com.sourcesense.joyce.core.exception.InvalidJoyceUriException;
import com.sourcesense.joyce.core.exception.SchemaNotFoundException;
import com.sourcesense.joyce.core.model.JoyceURI;
import com.sourcesense.joyce.core.service.ContentProducer;
import com.sourcesense.joyce.core.service.SchemaService;
import com.sourcesense.joyce.importcore.exception.ImportException;
import com.sourcesense.joyce.importcore.service.ImportService;
import com.sourcesense.joyce.schemaengine.exception.InvalidSchemaException;
import com.sourcesense.joyce.schemaengine.exception.JoyceSchemaEngineException;
import com.sourcesense.joyce.schemaengine.service.SchemaEngine;
import io.micrometer.core.instrument.util.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ImportServiceTest {

	// Mocked components
	@Mock
	private SchemaEngine schemaEngine;
	@Mock
	private SchemaService schemaService;
	@Mock
	private ContentProducer contentProducer;

	private final ObjectMapper objectMapper = new ObjectMapper();

	// Subject under test
	private ImportService importService;

	// CONSTANTS
	private static final String MESSAGE_KEY = "joyce://raw/other/user/1";
	private static final String INVALID_MESSAGE_KEY = "mississippi://raw/other/user/1";
	private static final String IMPORT_SCHEMA = "joyce://schema/import/user";
	private static final String INVALID_IMPORT_SCHEMA = "joyce://schema/not-import/user";

	private static final String TEST_USER_JSON = "test-user.json";
	private static final String TEST_SCHEMA_JSON_USER = "test-schema-user.json";
	private static final String TEST_SCHEMA_JSON_ENHANCED_USER = "test-schema-enhanced-user.json";

	private static final String TEST_COMPLEX_MESSAGE_KEY_CORRECT = "complex-message/test-complex-message-key-correct.json";
	private static final String TEST_COMPLEX_MESSAGE_KEY_MISSING_SCHEMA = "complex-message/test-complex-message-key-missing-schema.json";
	private static final String TEST_COMPLEX_MESSAGE_KEY_MISSING_UID = "complex-message/test-complex-message-key-missing-uid.json";
	private static final String TEST_COMPLEX_MESSAGE_KEY_MISSING_SOURCE = "complex-message/test-complex-message-key-missing-source.json";

	@BeforeEach
	void init() {
		importService = new ImportService(objectMapper, schemaEngine, schemaService, contentProducer);
	}

	/*
	computeRawUri tests
 */
	@Test
	void shouldComputeRawUriWhenImportSchemaIsNotNull() throws JsonProcessingException {
		Map<String, String> headers = this.computeHeaders(IMPORT_SCHEMA);

		JoyceURI expectedRawUri = JoyceURI.createURI(MESSAGE_KEY).get();
		JoyceURI actualRawUri = importService.computeRawURI(MESSAGE_KEY, headers);

		assertEquals(expectedRawUri, actualRawUri);
	}

	@Test
	void shouldComputeRawUriWhenImportSchemaIsNull() throws JsonProcessingException {
		Map<String, String> headers = this.computeHeaders(null);
		String messageKey = IOUtils.toString(this.computeJsonFromResource(TEST_COMPLEX_MESSAGE_KEY_CORRECT));

		JoyceURI expectedRawUri = JoyceURI.createURI(MESSAGE_KEY).get();
		JoyceURI actualRawUri = importService.computeRawURI(messageKey, headers);

		assertEquals(expectedRawUri, actualRawUri);
	}

	@Test
	void shouldThrowInvalidJoyceURIExceptionWhenJoyceUriIsInvalid() {
		Map<String, String> headers = this.computeHeaders(IMPORT_SCHEMA);

		assertThrows(
				InvalidJoyceUriException.class,
				() -> importService.computeRawURI(INVALID_MESSAGE_KEY, headers)
		);
	}

	/*
			computeValidSchemaUri tests
	 */
	@Test
	void shouldComputeValidSchemaUriWhenImportSchemaIsNotNull() throws JsonProcessingException {
		Map<String, String> headers = this.computeHeaders(IMPORT_SCHEMA);

		JoyceURI expectedSchemaUri = JoyceURI.createURI(IMPORT_SCHEMA).get();
		JoyceURI actualSchemaUri = importService.computeValidSchemaUri(null, headers, null);

		assertEquals(expectedSchemaUri, actualSchemaUri);
	}

	@Test
	void shouldComputeValidSchemaUriWhenImportSchemaIsNull() throws JsonProcessingException {
		Map<String, String> headers = this.computeHeaders(null);
		String messageKey = IOUtils.toString(this.computeJsonFromResource(TEST_COMPLEX_MESSAGE_KEY_CORRECT));

		JoyceURI expectedSchemaUri = JoyceURI.createURI(IMPORT_SCHEMA).get();
		JoyceURI actualSchemaUri = importService.computeValidSchemaUri(messageKey, headers, null);

		assertEquals(expectedSchemaUri, actualSchemaUri);
	}

	@Test
	void shouldThrowInvalidJoyceURIExceptionIfSubtypeIsNotImport() {
		Map<String, String> headers = this.computeHeaders(INVALID_IMPORT_SCHEMA);

		assertThrows(
				InvalidJoyceUriException.class,
				() -> importService.computeValidSchemaUri(null, headers, null)
		);
	}

	/*
		checkValidKey tests
	 */
	@Test
	void shouldThrowImportExceptionWhenSchemaIsMissing() {
		Map<String, String> headers = this.computeHeaders(null);
		String messageKey = IOUtils.toString(this.computeJsonFromResource(TEST_COMPLEX_MESSAGE_KEY_MISSING_SCHEMA));

		assertThrows(
				ImportException.class,
				() -> importService.computeRawURI(messageKey, headers)
		);

		assertThrows(
				ImportException.class,
				() -> importService.computeValidSchemaUri(messageKey, headers, null)
		);
	}

	@Test
	void shouldThrowImportExceptionWhenUidIsMissing() {
		Map<String, String> headers = this.computeHeaders(null);
		String messageKey = IOUtils.toString(this.computeJsonFromResource(TEST_COMPLEX_MESSAGE_KEY_MISSING_UID));

		assertThrows(
				ImportException.class,
				() -> importService.computeRawURI(messageKey, headers)
		);

		assertThrows(
				ImportException.class,
				() -> importService.computeValidSchemaUri(messageKey, headers, null)
		);
	}

	@Test
	void shouldThrowImportExceptionWhenSourceIsMissing() {
		Map<String, String> headers = this.computeHeaders(null);
		String messageKey = IOUtils.toString(this.computeJsonFromResource(TEST_COMPLEX_MESSAGE_KEY_MISSING_SOURCE));

		assertThrows(
				ImportException.class,
				() -> importService.computeRawURI(messageKey, headers)
		);

		assertThrows(
				ImportException.class,
				() -> importService.computeValidSchemaUri(messageKey, headers, null)
		);
	}

	/*
			computeSchema tests
	 */
	@Test
	void shouldThrowSchemaNotFoundExceptionIfMissing() {
		JoyceURI schemaUri = JoyceURI.createURI(IMPORT_SCHEMA).get();

		when(schemaService.findByName(any())).thenThrow(SchemaNotFoundException.class);

		assertThrows(
				SchemaNotFoundException.class,
				() -> importService.computeSchema(schemaUri, null)
		);
	}

	@Test
	void shouldProcessImportWhenTheInputDataIsCorrect() throws IOException {

		// mocking and stubbing data for test execution
		Schema schema = computeSchema(TEST_SCHEMA_JSON_USER);
		JoyceURI rawURI = JoyceURI.createURI(MESSAGE_KEY).orElseThrow();
		when(schemaEngine.process(any(), any(), any()))
				.thenReturn(objectMapper.valueToTree(Map.of("code", "1337")));

		// Subject under test
		boolean result = importService.processImport(rawURI, computeDocument(), schema);

		// Asserts
		assertTrue(result);
	}

	@Test
	void shouldThrowInvalidMetadataExceptionIfSchemaHasNotMetadata() throws IOException {
		// mocking and stubbing data for test execution
		Schema schema = new Schema();

		// asserts
		assertThrows(
				InvalidMetadataException.class,
				() -> importService.processImport(null, null, schema)
		);
	}

	@Test
	void shouldThrowSchemaIsNotValidExceptionIfSchemaIsNotValidated() throws IOException {
		// mocking and stubbing data for test execution
		Schema schema = computeSchema(TEST_SCHEMA_JSON_USER);
		when(schemaEngine.process(any(), any(), any()))
				.thenThrow(InvalidSchemaException.class);

		// asserts
		assertThrows(InvalidSchemaException.class, () -> importService.processImport(null, null, schema));
	}

	@Test
	void shouldThrowInvalidSchemaExceptionIfSchemaHasParentButParentSchemaIsNotAlreadyPresentInDb() throws IOException {
		// mocking and stubbing data for test execution
		Schema schema = computeSchema(TEST_SCHEMA_JSON_ENHANCED_USER);

		// asserts
		assertThrows(JoyceSchemaEngineException.class, () -> importService.processImport(null, null, schema));
	}

	@Test
	void shouldProcessImportIfSchemaHasParentAndParentSchemaIsAlreadyPresentInDb() throws IOException {
		// mocking and stubbing data for test execution
		Schema schema = computeSchema(TEST_SCHEMA_JSON_ENHANCED_USER);
		JoyceURI rawURI = JoyceURI.createURI(MESSAGE_KEY).orElseThrow();
		when(schemaEngine.process(any(), any(), any()))
				.thenReturn(objectMapper.valueToTree(Map.of("code", "1337")));
		when(schemaService.get(any()))
				.thenReturn(Optional.of(computeSchema(TEST_SCHEMA_JSON_USER)));

		// asserts
		assertTrue(importService.processImport(null, null, schema));
	}

	/* UTILITY METHODS */
	private Schema computeSchema(String jsonSchemaName) throws IOException {
		return objectMapper.readValue(computeJsonFromResource(jsonSchemaName), Schema.class);
	}

	/* UTILITY METHODS */
	private Map<String, String> computeHeaders(String importSchema) {
		return Objects.nonNull(importSchema)
				? Map.of(KafkaCustomHeaders.IMPORT_SCHEMA, importSchema)
				: new HashMap<>();
	}

	private JsonNode computeDocument() throws IOException {
		return objectMapper.readTree(computeJsonFromResource(TEST_USER_JSON));
	}

	private InputStream computeJsonFromResource(String jsonFileName) {
		return this.getClass().getClassLoader().getResourceAsStream(jsonFileName);
	}
}
