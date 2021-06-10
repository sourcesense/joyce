package com.sourcesense.joyce.sink.mongodb.service;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.client.model.IndexOptions;
import com.sourcesense.joyce.core.annotation.ContentUri;
import com.sourcesense.joyce.core.annotation.EventPayload;
import com.sourcesense.joyce.core.annotation.Notify;
import com.sourcesense.joyce.core.enumeration.NotificationEvent;
import com.sourcesense.joyce.core.model.SchemaEntity;
import com.sourcesense.joyce.sink.mongodb.model.MetadataIndexesProperties;
import com.sourcesense.joyce.sink.mongodb.model.MongoIndex;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class CollectionEnhancer {

	private final ObjectMapper mapper;
	private final MongoTemplate mongoTemplate;
	private final MetadataIndexesProperties metadataIndexesProperties;

	@Notify(
			successEvent = NotificationEvent.SINK_MONGODB_CREATE_COLLECTION_SUCCESS,
			failureEvent = NotificationEvent.SINK_MONGODB_CREATE_COLLECTION_FAILED
	)
	public void initCollection(
			@ContentUri String schemaUri,
			@EventPayload SchemaEntity schema) {

		log.debug("Creating collection '{}' for schema '{}'", schema.getMetadata().getCollection(), schemaUri);
		if (!mongoTemplate.collectionExists(schema.getMetadata().getCollection())) {
			mongoTemplate.createCollection(schema.getMetadata().getCollection());
		}
	}

	@Notify(
			successEvent = NotificationEvent.SINK_MONGODB_UPDATE_VALIDATION_SCHEMA_SUCCESS,
			failureEvent = NotificationEvent.SINK_MONGODB_UPDATE_VALIDATION_SCHEMA_FAILED
	)
	public void upsertCollectionValidator(
			@ContentUri String schemaUri,
			@EventPayload SchemaEntity schema) throws JsonProcessingException {

		log.debug("Updating validation schema for schema: '{}'", schemaUri);
		LinkedHashMap<String, Object> validatorCommand = new LinkedHashMap<>();
		validatorCommand.put("collMod", schema.getMetadata().getCollection());
		validatorCommand.put("validator", this.computeValidationSchema(schema));
		mongoTemplate.executeCommand(new Document(validatorCommand));
	}

	@Notify(
			successEvent = NotificationEvent.SINK_MONGODB_CREATE_INDEXES_SUCCESS,
			failureEvent = NotificationEvent.SINK_MONGODB_CREATE_INDEXES_FAILED
	)
	public void createIndexes(
			@ContentUri String schemaUri,
			@EventPayload SchemaEntity schema) {

		log.debug("Creating indexes for schema: '{}'", schemaUri);
		List<Map<String, Object>> fieldIndexes = schema.getMetadata().getIndexes();
		this.insertIndexes(
				this.computeMongoIndexes(fieldIndexes),
				schema.getMetadata().getCollection()
		);
	}

	private Document computeValidationSchema(SchemaEntity schema) throws JsonProcessingException {

		ObjectNode jsonValidationSchema = mapper.createObjectNode();
		jsonValidationSchema.putPOJO("type", schema.getType());
		this.putRequiredInValidationSchema(schema, jsonValidationSchema);
		this.putPropertiesInValidationSchema(schema, jsonValidationSchema);

		String stringValidationSchema = mapper
				.writeValueAsString(jsonValidationSchema)
				.replace("\"type\":\"integer\"", "\"type\":\"int\"")
				.replace("\"type\":", "\"bsonType\":");

		return new Document(
				"$jsonSchema", mapper.readValue(stringValidationSchema, Document.class)
		);
	}

	private void putRequiredInValidationSchema(SchemaEntity schema, ObjectNode jsonValidationSchema) {
		Optional.of(schema)
				.map(SchemaEntity::getRequired)
				.filter(Predicate.not(List::isEmpty))
				.ifPresent(required -> jsonValidationSchema.putPOJO("required", required));
	}

	private void putPropertiesInValidationSchema(SchemaEntity schema, ObjectNode jsonValidationSchema) {
		Optional.of(schema)
				.map(SchemaEntity::getProperties)
				.map(this::computeProperties)
				.ifPresent(properties -> jsonValidationSchema.putPOJO("properties", properties));
	}

	private Map<String, Map<String, Object>> computeProperties(JsonNode propertiesObject) {
		TypeReference<Map<String, Map<String, Object>>> jsonAsMap = new TypeReference<>() {};
		return mapper.convertValue(propertiesObject, jsonAsMap)
				.entrySet().stream()
				.collect(Collectors.toMap(
						Map.Entry::getKey,
						property -> this.computePropertyWithoutCustomHandlers(property.getValue())
						)
				);
	}

	private Map<String, Object> computePropertyWithoutCustomHandlers(Map<String, Object> property) {
		return property.entrySet().stream()
				.filter(propertyField -> !propertyField.getKey().startsWith("$"))
				.collect(Collectors.toMap(
						Map.Entry::getKey,
						Map.Entry::getValue
						)
				);
	}

	private List<MongoIndex> computeMongoIndexes(List<Map<String, Object>> fieldIndexes) {
		return Stream.of(
				this.getMongoIndexesOrElseEmptyList(metadataIndexesProperties.getMetadataIndexes()),
				this.getMongoIndexesOrElseEmptyList(fieldIndexes)
		)
				.flatMap(List::stream)
				.flatMap(this::buildMongoIndex)
				.collect(Collectors.toList());
	}

	private List<Map<String, Object>> getMongoIndexesOrElseEmptyList(List<Map<String, Object>> indexes) {
		return Optional.ofNullable(indexes).orElse(Collections.emptyList());
	}

	private Stream<MongoIndex> buildMongoIndex(Map<String, Object> fields) {
		return fields.keySet().stream()
				.reduce((f1, f2) -> f1.concat("_").concat(f2))
				.filter(Predicate.not(String::isEmpty))
				.map(name -> MongoIndex.builder().name(name).fields(fields).build())
				.stream();
	}

	private void insertIndexes(List<MongoIndex> mongoIndexes, String collection) {
		List<String> existingIndexesNames = this.computeExistingIndexesNames(collection);
		mongoIndexes.stream()
				.filter(index -> !existingIndexesNames.contains(index.getName()))
				.forEach(index -> {
							log.debug("Inserting index with name '{}' in collection '{}'", index.getName(), collection);
							Document mongoIndex = new Document(index.getFields());
							IndexOptions indexOptions = new IndexOptions().name(index.getName());
							mongoTemplate.getCollection(collection).createIndex(mongoIndex, indexOptions);
						}
				);
	}

	private List<String> computeExistingIndexesNames(String collection) {
		log.debug("Retrieving existing indexes from '{}' collection", collection);
		List<Document> existingIndexes = new ArrayList<>();
		mongoTemplate.getDb()
				.getCollection(collection)
				.listIndexes()
				.iterator()
				.forEachRemaining(existingIndexes::add);

		return existingIndexes.stream()
				.map(document -> document.get("name"))
				.map(String.class::cast)
				.collect(Collectors.toList());
	}
}
