package com.sourcesense.joyce.sink.mongodb.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.sourcesense.joyce.core.configuration.mongo.MongodbProperties;
import com.sourcesense.joyce.sink.mongodb.model.JsonSchemaEntry;
import com.sourcesense.joyce.sink.mongodb.model.MongoIndex;
import com.sourcesense.joyce.sink.mongodb.test.MongodbSinkJoyceTest;
import org.bson.Document;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class CollectionEnhancerTest extends MongodbSinkJoyceTest {

	private static final String SCHEMA_COLLECTION = "joyce_schema";


	@Test
	void shouldConvertAllTypes() throws URISyntaxException, IOException {
		this.compareSchemaAndValidator("schema/31.json", "validator/31.json");
	}

	@Test
	void shouldStripEmptyOrNullRequired() throws URISyntaxException, IOException {
		this.compareSchemaAndValidator("schema/32.json", "validator/32.json");
	}

	@Test
	void shouldStripCustomHandlers() throws IOException, URISyntaxException {
		this.compareSchemaAndValidator("schema/33.json", "validator/33.json");
	}

	@Test
	void shouldConvertFullSchema() throws IOException, URISyntaxException {
		this.compareSchemaAndValidator("schema/34.json", "validator/34.json");
	}

	@Test
	void shouldComputeMetadataIndexes() {
		Map<String, Object> sourceURI = Map.of("source_uri", 1);
		Map<String, Object> schemaURI = Map.of("schema_uri", 1);
		Map<String, Object> sourceURISchemaURI = new LinkedHashMap<>();
		sourceURISchemaURI.put("source_uri", 1);
		sourceURISchemaURI.put("schema_uri", 1);

		MongodbProperties mongodbProperties = new MongodbProperties(
				true,
				SCHEMA_COLLECTION,
				List.of(sourceURI, schemaURI, sourceURISchemaURI)
		);

		List<MongoIndex> actual = this.computeMongoIndexes(
				Collections.emptyList(),
				mongodbProperties
		);

		List<MongoIndex> expected = List.of(
				new MongoIndex("source_uri", sourceURI),
				new MongoIndex("schema_uri", schemaURI),
				new MongoIndex("source_uri_schema_uri", sourceURISchemaURI)
		);

		assertThat(expected).hasSameElementsAs(actual);
	}

	@Test
	void shouldComputeFieldIndexes() {
		Map<String, Object> sourceURI = Map.of("source_uri", 1);
		Map<String, Object> schemaURI = Map.of("schema_uri", 1);
		Map<String, Object> sourceURISchemaURI = new LinkedHashMap<>();
		sourceURISchemaURI.put("source_uri", 1);
		sourceURISchemaURI.put("schema_uri", 1);

		List<Map<String, Object>> fieldIndexes = List.of(sourceURI, schemaURI, sourceURISchemaURI);
		List<MongoIndex> actual = this.computeMongoIndexes(fieldIndexes, new MongodbProperties());

		List<MongoIndex> expected = List.of(
				new MongoIndex("source_uri", sourceURI),
				new MongoIndex("schema_uri", schemaURI),
				new MongoIndex("source_uri_schema_uri", sourceURISchemaURI)
		);

		assertThat(expected).hasSameElementsAs(actual);

	}

	@Test
	void shouldComputeMixedIndexes() {
		Map<String, Object> fullName = Map.of("full_name", 1);
		Map<String, Object> sourceURI = Map.of("source_uri", 1);

		List<Map<String, Object>> fieldIndexes = List.of(fullName);

		MongodbProperties mongodbProperties = new MongodbProperties(
				true,
				SCHEMA_COLLECTION,
				List.of(sourceURI)
		);

		List<MongoIndex> actual = this.computeMongoIndexes(fieldIndexes, mongodbProperties);
		List<MongoIndex> expected = List.of(
				new MongoIndex("full_name", fullName),
				new MongoIndex("source_uri", sourceURI)
		);

		assertThat(expected).hasSameElementsAs(actual);
	}

	private void compareSchemaAndValidator(String schemaPath, String validatorPath) throws URISyntaxException, IOException {
		String schemaJson = this.computeResourceAsString(schemaPath);
		String validatorJson = this.computeResourceAsString(validatorPath);

		JsonSchemaEntry jsonSchemaEntry = jsonMapper.readValue(schemaJson, JsonSchemaEntry.class);
		Map<String, Object> validatorMap = jsonMapper.readValue(validatorJson, new TypeReference<>() {
		});

		CollectionEnhancerService collectionEnhancerService = new CollectionEnhancerService(jsonMapper, null, null, null);

		Document expected = new Document("$jsonSchema", new Document(validatorMap));
		Document actual = ReflectionTestUtils.invokeMethod(collectionEnhancerService, "computeValidationSchema", jsonSchemaEntry);

		assertEquals(expected, actual);
	}

	private List<MongoIndex> computeMongoIndexes(
			List<Map<String, Object>> fieldIndexes,
			MongodbProperties mongodbProperties) {

		CollectionEnhancerService collectionEnhancerService = new CollectionEnhancerService(jsonMapper, null, null, mongodbProperties);
		return ReflectionTestUtils.invokeMethod(collectionEnhancerService, "computeMongoIndexes", fieldIndexes);
	}
}
