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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.sourcesense.joyce.core.exception.InvalidMetadataException;
import com.sourcesense.joyce.core.model.entity.SchemaEntity;
import com.sourcesense.joyce.core.model.uri.JoyceSourceURI;
import com.sourcesense.joyce.core.model.uri.JoyceURIFactory;
import com.sourcesense.joyce.core.producer.ContentProducer;
import com.sourcesense.joyce.core.service.CsvMappingService;
import com.sourcesense.joyce.core.utililty.SchemaUtils;
import com.sourcesense.joyce.importcore.enumeration.ProcessStatus;
import com.sourcesense.joyce.importcore.model.dto.SingleImportResult;
import com.sourcesense.joyce.importcore.test.ImportCoreJoyceTest;
import com.sourcesense.joyce.schemacore.service.SchemaService;
import com.sourcesense.joyce.schemaengine.exception.InvalidSchemaException;
import com.sourcesense.joyce.schemaengine.exception.JoyceSchemaEngineException;
import com.sourcesense.joyce.schemaengine.service.SchemaEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ImportServiceTest extends ImportCoreJoyceTest {

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

	// Subject under test
	private ImportService importService;

	// CONSTANTS
	private static final String SINGLE_REST_URI = "joyce:content:test:default:user:src:rest:single:666";
	private static final String BULK_REST_URI = "joyce:content:test:default:user:src:rest:bulk:666.csv";

	private static final JoyceSourceURI JOYCE_SINGLE_REST_URI = JoyceURIFactory.getInstance().createURIOrElseThrow(SINGLE_REST_URI, JoyceSourceURI.class);
	private static final JoyceSourceURI JOYCE_BULK_REST_URI = JoyceURIFactory.getInstance().createURIOrElseThrow(BULK_REST_URI, JoyceSourceURI.class);

	private static final String TEST_USER_JSON = "test-user.json";
	private static final String TEST_SCHEMA_JSON_USER = "test-schema-user.json";
	private static final String TEST_SCHEMA_JSON_ENHANCED_USER = "test-schema-enhanced-user.json";

	@BeforeEach
	void init() {
		importService = new ImportService(jsonMapper, schemaUtils, schemaService, contentProducer, jsonLogicService, csvMappingService, schemaEngine);
	}

	@Test
	void shouldProcessImportWhenTheInputDataIsCorrect() throws IOException, URISyntaxException {

		// mocking and stubbing data for test execution
		SchemaEntity schema = this.computeResourceAsObject(TEST_SCHEMA_JSON_USER, SchemaEntity.class);
		when(schemaUtils.metadataFromSchemaOrElseThrow(any())).thenReturn(schema.getMetadata());
		when(jsonLogicService.filter(any(), any())).thenReturn(true);
		when(schemaEngine.process(any(SchemaEntity.class), any()))
				.thenReturn(jsonMapper.valueToTree(Map.of("code", "1337")));

		// Subject under test
		SingleImportResult expected = new SingleImportResult(JOYCE_SINGLE_REST_URI, ProcessStatus.IMPORTED, null);
		SingleImportResult actual = importService.processImport(JOYCE_SINGLE_REST_URI, this.computeResourceAsNode(TEST_USER_JSON), schema);

		// Asserts
		assertEquals(expected, actual);
	}

	@Test
	void shouldThrowInvalidMetadataExceptionIfSchemaHasNotMetadata() {
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
	void shouldThrowSchemaIsNotValidExceptionIfSchemaIsNotValidated() throws IOException, URISyntaxException {
		// mocking and stubbing data for test execution
		SchemaEntity schema = this.computeResourceAsObject(TEST_SCHEMA_JSON_USER, SchemaEntity.class);
		when(jsonLogicService.filter(any(), any())).thenReturn(true);
		when(schemaEngine.process(any(SchemaEntity.class), any()))
				.thenThrow(InvalidSchemaException.class);

		// asserts
		assertThrows(InvalidSchemaException.class, () -> importService.processImport(null, null, schema));
	}

	@Test
	void shouldThrowInvalidSchemaExceptionIfSchemaHasParentButParentSchemaIsNotAlreadyPresentInDb() throws IOException, URISyntaxException {
		// mocking and stubbing data for test execution
		SchemaEntity schema = this.computeResourceAsObject(TEST_SCHEMA_JSON_ENHANCED_USER, SchemaEntity.class);
		when(schemaUtils.metadataFromSchemaOrElseThrow(any())).thenReturn(schema.getMetadata());
		when(jsonLogicService.filter(any(), any())).thenReturn(true);

		// asserts
		assertThrows(JoyceSchemaEngineException.class, () -> importService.processImport(null, null, schema));
	}

	@Test
	void shouldProcessImportIfSchemaHasParentAndParentSchemaIsAlreadyPresentInDb() throws IOException, URISyntaxException {
		// mocking and stubbing data for test execution
		SchemaEntity parentSchema = this.computeResourceAsObject(TEST_SCHEMA_JSON_USER, SchemaEntity.class);
		SchemaEntity childSchema = this.computeResourceAsObject(TEST_SCHEMA_JSON_ENHANCED_USER, SchemaEntity.class);

		when(schemaUtils.metadataFromSchemaOrElseThrow(any())).thenReturn(childSchema.getMetadata());
		when(jsonLogicService.filter(any(), any())).thenReturn(true);
		when(schemaEngine.process(any(SchemaEntity.class), any()))
				.thenReturn(jsonMapper.valueToTree(Map.of("code", "1337")));
		when(schemaService.get(any()))
				.thenReturn(Optional.of(parentSchema));

		SingleImportResult expected = new SingleImportResult(JOYCE_SINGLE_REST_URI, ProcessStatus.IMPORTED, null);
		SingleImportResult actual = importService.processImport(JOYCE_SINGLE_REST_URI, null, childSchema);

		// asserts
		assertEquals(expected, actual);
	}

	@Test
	void shouldRetrieveCsvRowsFromFile() throws IOException, URISyntaxException {

		MultipartFile multipartFile = new MockMultipartFile("01", "01.csv", "text/csv", new byte[0]);

		List<JsonNode> expected = this.computeResourceAsObject("result/bulk/csv/01.json", new TypeReference<>() {});

		when(csvMappingService.convertCsvFileToDocuments(any(), any(), any())).thenReturn(expected);

		List<JsonNode> actual = importService.computeDocumentsFromFile(JOYCE_BULK_REST_URI, multipartFile, ',', ";");
		assertThat(expected).hasSameElementsAs(actual);
	}
}
