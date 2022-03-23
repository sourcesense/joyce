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

package com.sourcesense.joyce.importcore.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sourcesense.joyce.core.enumeration.KafkaCustomHeaders;
import com.sourcesense.joyce.core.exception.InvalidJoyceURIException;
import com.sourcesense.joyce.core.exception.InvalidMetadataException;
import com.sourcesense.joyce.core.exception.SchemaNotFoundException;
import com.sourcesense.joyce.core.utililty.SchemaUtils;
import com.sourcesense.joyce.core.model.JoyceURI;
import com.sourcesense.joyce.core.model.entity.SchemaEntity;
import com.sourcesense.joyce.core.producer.ContentProducer;
import com.sourcesense.joyce.core.service.CsvMappingService;
import com.sourcesense.joyce.schemacore.service.SchemaService;
import com.sourcesense.joyce.importcore.dto.SingleImportResult;
import com.sourcesense.joyce.importcore.enumeration.ProcessStatus;
import com.sourcesense.joyce.importcore.exception.ImportException;
import com.sourcesense.joyce.schemaengine.exception.InvalidSchemaException;
import com.sourcesense.joyce.schemaengine.exception.JoyceSchemaEngineException;
import com.sourcesense.joyce.schemaengine.service.SchemaEngine;
import io.micrometer.core.instrument.util.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ImportServiceTest {

	// Mocked components
	@Mock
	private SchemaUtils schemaUtils;
	@Mock
	private SchemaEngine<SchemaEntity> schemaEngine;
	@Mock
	private SchemaService schemaService;
	@Mock
	private ContentProducer contentProducer;
	@Mock
	private JsonLogicService jsonLogicService;
	@Mock
	private CsvMappingService csvMappingService;

	private final ObjectMapper objectMapper = new ObjectMapper();

	// Subject under test
	private ImportService importService;

	// CONSTANTS
	private static final String MESSAGE_KEY = "joyce://raw/other/user/1";
	private static final String BULK_CSV_MESSAGE_KEY = "joyce://raw/rest/bulk/01.csv";
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
		importService = new ImportService(objectMapper, schemaUtils, schemaService, contentProducer, jsonLogicService, csvMappingService, schemaEngine);
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
		String messageKey = this.computeResourceAsString(TEST_COMPLEX_MESSAGE_KEY_CORRECT);

		JoyceURI expectedRawUri = JoyceURI.createURI(MESSAGE_KEY).get();
		JoyceURI actualRawUri = importService.computeRawURI(messageKey, headers);

		assertEquals(expectedRawUri, actualRawUri);
	}

	@Test
	void shouldThrowInvalidJoyceURIExceptionWhenJoyceUriIsInvalid() {
		Map<String, String> headers = this.computeHeaders(IMPORT_SCHEMA);

		assertThrows(
				InvalidJoyceURIException.class,
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
		String messageKey = this.computeResourceAsString(TEST_COMPLEX_MESSAGE_KEY_CORRECT);

		JoyceURI expectedSchemaUri = JoyceURI.createURI(IMPORT_SCHEMA).get();
		JoyceURI actualSchemaUri = importService.computeValidSchemaUri(messageKey, headers, null);

		assertEquals(expectedSchemaUri, actualSchemaUri);
	}

	@Test
	void shouldThrowInvalidJoyceURIExceptionIfSubtypeIsNotImport() {
		Map<String, String> headers = this.computeHeaders(INVALID_IMPORT_SCHEMA);

		assertThrows(
				InvalidJoyceURIException.class,
				() -> importService.computeValidSchemaUri(null, headers, null)
		);
	}

	/*
		checkValidKey tests
	 */
	@Test
	void shouldThrowImportExceptionWhenSchemaIsMissing() {
		Map<String, String> headers = this.computeHeaders(null);
		String messageKey = this.computeResourceAsString(TEST_COMPLEX_MESSAGE_KEY_MISSING_SCHEMA);

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
		String messageKey = this.computeResourceAsString(TEST_COMPLEX_MESSAGE_KEY_MISSING_UID);

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
		String messageKey = this.computeResourceAsString(TEST_COMPLEX_MESSAGE_KEY_MISSING_SOURCE);

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

		when(schemaService.getOrElseThrow(any(), any(), any())).thenThrow(SchemaNotFoundException.class);

		assertThrows(
				SchemaNotFoundException.class,
				() -> importService.computeSchema(schemaUri, null)
		);
	}

	@Test
	void shouldProcessImportWhenTheInputDataIsCorrect() throws IOException {

		// mocking and stubbing data for test execution
		SchemaEntity schema = computeSchema(TEST_SCHEMA_JSON_USER);
		JoyceURI rawURI = JoyceURI.createURI(MESSAGE_KEY).orElseThrow();
		when(schemaUtils.metadataFromSchemaOrElseThrow(any())).thenReturn(schema.getMetadata());
		when(jsonLogicService.filter(any(), any())).thenReturn(true);
		when(schemaEngine.process(any(SchemaEntity.class), any(), any()))
				.thenReturn(objectMapper.valueToTree(Map.of("code", "1337")));

		// Subject under test
		SingleImportResult expected = new SingleImportResult(rawURI, ProcessStatus.IMPORTED, null);
		SingleImportResult actual = importService.processImport(rawURI, computeDocument(TEST_USER_JSON), schema);

		// Asserts
		assertEquals(expected, actual);
	}

	@Test
	void shouldThrowInvalidMetadataExceptionIfSchemaHasNotMetadata() throws IOException {
		// mocking and stubbing data for test execution
		SchemaEntity schema = new SchemaEntity();

		when(schemaUtils.metadataFromSchemaOrElseThrow(any())).thenThrow(InvalidMetadataException.class);

		// asserts
		assertThrows(
				InvalidMetadataException.class,
				() -> importService.processImport(null, null, schema)
		);
	}

	@Test
	void shouldThrowSchemaIsNotValidExceptionIfSchemaIsNotValidated() throws IOException {
		// mocking and stubbing data for test execution
		SchemaEntity schema = computeSchema(TEST_SCHEMA_JSON_USER);
		when(jsonLogicService.filter(any(), any())).thenReturn(true);
		when(schemaEngine.process(any(SchemaEntity.class), any(), any()))
				.thenThrow(InvalidSchemaException.class);

		// asserts
		assertThrows(InvalidSchemaException.class, () -> importService.processImport(null, null, schema));
	}

	@Test
	void shouldThrowInvalidSchemaExceptionIfSchemaHasParentButParentSchemaIsNotAlreadyPresentInDb() throws IOException {
		// mocking and stubbing data for test execution
		SchemaEntity schema = computeSchema(TEST_SCHEMA_JSON_ENHANCED_USER);
		when(schemaUtils.metadataFromSchemaOrElseThrow(any())).thenReturn(schema.getMetadata());
		when(jsonLogicService.filter(any(), any())).thenReturn(true);

		// asserts
		assertThrows(JoyceSchemaEngineException.class, () -> importService.processImport(null, null, schema));
	}

	@Test
	void shouldProcessImportIfSchemaHasParentAndParentSchemaIsAlreadyPresentInDb() throws IOException {
		// mocking and stubbing data for test execution
		SchemaEntity schema = computeSchema(TEST_SCHEMA_JSON_ENHANCED_USER);
		JoyceURI rawURI = JoyceURI.createURI(MESSAGE_KEY).orElseThrow();
		when(schemaUtils.metadataFromSchemaOrElseThrow(any())).thenReturn(schema.getMetadata());
		when(jsonLogicService.filter(any(), any())).thenReturn(true);
		when(schemaEngine.process(any(SchemaEntity.class), any(), any()))
				.thenReturn(objectMapper.valueToTree(Map.of("code", "1337")));
		when(schemaService.get(any()))
				.thenReturn(Optional.of(computeSchema(TEST_SCHEMA_JSON_USER)));

		SingleImportResult expected = new SingleImportResult(rawURI, ProcessStatus.IMPORTED, null);
		SingleImportResult actual = importService.processImport(rawURI, null, schema);

		// asserts
		assertEquals(expected, actual);
	}

	@Test
	void shouldRetrieveCsvRowsFromFile() throws IOException, URISyntaxException {

		JoyceURI rawURI = JoyceURI.createURI(BULK_CSV_MESSAGE_KEY).orElseThrow();
		MultipartFile multipartFile = new MockMultipartFile("01", "01.csv", "text/csv", new byte[0]);

		List<JsonNode> expected = this.computeResourceAsNodeList("result/bulk/csv/01.json");

		when(csvMappingService.convertCsvFileToDocuments(any(), any(), any())).thenReturn(expected);

		List<JsonNode> actual = importService.computeDocumentsFromFile(rawURI, multipartFile, ',', ";");
		assertThat(expected).hasSameElementsAs(actual);
	}

	/* UTILITY METHODS */
	private SchemaEntity computeSchema(String jsonSchemaName) throws IOException {
		InputStream resource = this.computeResourceAsBytes(jsonSchemaName);
		return objectMapper.readValue(resource, SchemaEntity.class);
	}

	/* UTILITY METHODS */
	private Map<String, String> computeHeaders(String importSchema) {
		return Objects.nonNull(importSchema)
				? Map.of(KafkaCustomHeaders.IMPORT_SCHEMA, importSchema)
				: new HashMap<>();
	}

	private JsonNode computeDocument(String path) throws IOException {
		return objectMapper.readTree(
				this.computeResourceAsBytes(path)
		);
	}

	private List<JsonNode> computeResourceAsNodeList(String path) throws IOException {
		return objectMapper.readValue(
				this.computeResourceAsBytes(path),
				new TypeReference<>() {}
		);
	}

	private String computeResourceAsString(String path) {
		InputStream resourceStream = this.computeResourceAsBytes(path);
		return IOUtils.toString(resourceStream);
	}

	private InputStream computeResourceAsBytes(String path) {
		return this.getClass().getClassLoader().getResourceAsStream(path);
	}
}
