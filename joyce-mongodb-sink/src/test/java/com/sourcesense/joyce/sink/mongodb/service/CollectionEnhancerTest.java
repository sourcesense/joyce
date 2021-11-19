package com.sourcesense.joyce.sink.mongodb.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.sourcesense.joyce.sink.mongodb.model.MetadataIndexesProperties;
import com.sourcesense.joyce.sink.mongodb.model.MongoIndex;
import com.sourcesense.joyce.sink.mongodb.model.SchemaObject;
import org.bson.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class CollectionEnhancerTest implements ResourceLoader {

	private ObjectMapper mapper;

	@BeforeEach
	void init() {
		this.mapper = this.initMapper();
	}

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
		Map<String, Object> rawUri = Map.of("raw_uri", 1);
		Map<String, Object> schemaUid = Map.of("schema_uid", 1);
		Map<String, Object> rawUriSchemaUid = new LinkedHashMap<>();
		rawUriSchemaUid.put("raw_uri", 1);
		rawUriSchemaUid.put("schema_uid", 1);

		MetadataIndexesProperties metadataIndexesProperties = new MetadataIndexesProperties(
				List.of(rawUri, schemaUid, rawUriSchemaUid)
		);

		List<MongoIndex> actual = this.computeMongoIndexes(
				Collections.emptyList(),
				metadataIndexesProperties
		);

		List<MongoIndex> expected = List.of(
				new MongoIndex("raw_uri", rawUri),
				new MongoIndex("schema_uid", schemaUid),
				new MongoIndex("raw_uri_schema_uid", rawUriSchemaUid)
		);

		assertThat(expected).hasSameElementsAs(actual);
	}

	@Test
	void shouldComputeFieldIndexes() {
		Map<String, Object> rawUri = Map.of("raw_uri", 1);
		Map<String, Object> schemaUid = Map.of("schema_uid", 1);
		Map<String, Object> rawUriSchemaUid = new LinkedHashMap<>();
		rawUriSchemaUid.put("raw_uri", 1);
		rawUriSchemaUid.put("schema_uid", 1);

		List<Map<String, Object>> fieldIndexes = List.of(rawUri, schemaUid, rawUriSchemaUid);
		List<MongoIndex> actual = this.computeMongoIndexes(fieldIndexes, new MetadataIndexesProperties());

		List<MongoIndex> expected = List.of(
				new MongoIndex("raw_uri", rawUri),
				new MongoIndex("schema_uid", schemaUid),
				new MongoIndex("raw_uri_schema_uid", rawUriSchemaUid)
		);

		assertThat(expected).hasSameElementsAs(actual);

	}

	@Test
	void shouldComputeMixedIndexes() {
		Map<String, Object> fullName = Map.of("full_name", 1);
		Map<String, Object> rawUri = Map.of("raw_uri", 1);

		List<Map<String, Object>> fieldIndexes = List.of(fullName);

		MetadataIndexesProperties metadataIndexesProperties = new MetadataIndexesProperties(
				List.of(rawUri)
		);

		List<MongoIndex> actual = this.computeMongoIndexes(fieldIndexes, metadataIndexesProperties);
		List<MongoIndex> expected = List.of(
				new MongoIndex("full_name", fullName),
				new MongoIndex("raw_uri", rawUri)
		);

		assertThat(expected).hasSameElementsAs(actual);
	}

	private void compareSchemaAndValidator(String schemaPath, String validatorPath) throws URISyntaxException, IOException {
		String schemaJson = Files.readString(this.loadResource(schemaPath));
		String validatorJson = Files.readString(this.loadResource(validatorPath));

		SchemaObject schemaObject = mapper.readValue(schemaJson, SchemaObject.class);
		Map<String, Object> validatorMap = mapper.readValue(validatorJson, new TypeReference<>() {
		});

		CollectionEnhancer collectionEnhancer = new CollectionEnhancer(mapper, null, null, null);

		Document expected = new Document("$jsonSchema", new Document(validatorMap));
		Document actual = ReflectionTestUtils.invokeMethod(collectionEnhancer, "computeValidationSchema", schemaObject);

		assertEquals(expected, actual);
	}

	private List<MongoIndex> computeMongoIndexes(
			List<Map<String, Object>> fieldIndexes,
			MetadataIndexesProperties metadataIndexesProperties) {

		CollectionEnhancer collectionEnhancer = new CollectionEnhancer(mapper, null, null, metadataIndexesProperties);
		return ReflectionTestUtils.invokeMethod(collectionEnhancer, "computeMongoIndexes", fieldIndexes);
	}

	private ObjectMapper initMapper() {
		return new ObjectMapper()
				.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
				.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
	}
}
