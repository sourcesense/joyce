package com.sourcesense.joyce.sink.mongodb.service;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.model.IndexOptions;
import com.sourcesense.joyce.core.annotation.ContentUri;
import com.sourcesense.joyce.core.annotation.EventPayload;
import com.sourcesense.joyce.core.annotation.Notify;
import com.sourcesense.joyce.core.enumeration.NotificationEvent;
import com.sourcesense.joyce.core.mapper.SchemaMapper;
import com.sourcesense.joyce.core.model.SchemaEntity;
import com.sourcesense.joyce.sink.mongodb.exception.MongodbSinkException;
import com.sourcesense.joyce.sink.mongodb.model.MetadataIndexesProperties;
import com.sourcesense.joyce.sink.mongodb.model.MongoIndex;
import com.sourcesense.joyce.sink.mongodb.model.SchemaObject;
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
	private final SchemaMapper schemaMapper;
	private final MetadataIndexesProperties metadataIndexesProperties;

	/**
	 * This method reads a schema processed by ksqlDB and stored in kafka and converts it into
	 * a java object.
	 *
	 * @param schemaUri  Schema uri
	 * @param jsonSchema Schema's body
	 * @param clazz      Class object of the output java object
	 * @param <T>        Class of the output java object
	 * @return Converted schema
	 */
	@Notify(failureEvent = NotificationEvent.SINK_MONGODB_SCHEMA_PARSING_FAILED)
	public <T> T computeSchema(
			@ContentUri String schemaUri,
			@EventPayload JsonNode jsonSchema,
			Class<T> clazz) {

		log.debug("Parsing schema retrieved from kafka message");
		return schemaMapper.jsonToObject(jsonSchema, clazz)
				.orElseThrow(() -> new MongodbSinkException(
						"Impossible to parse schema from kafka message. " +
						(!schemaUri.isEmpty() ? "Schema uri is '" + schemaUri + "'" : schemaUri)
				));
	}

	/**
	 * This method creates a mongodb collection for the schema if the collection
	 * doesn't exist
	 *
	 * @param schemaUri Schema uri
	 * @param schema    Schema's body
	 */
	@Notify(
			successEvent = NotificationEvent.SINK_MONGODB_CREATE_COLLECTION_SUCCESS,
			failureEvent = NotificationEvent.SINK_MONGODB_CREATE_COLLECTION_FAILED
	)
	public void initCollection(
			@ContentUri String schemaUri,
			@EventPayload SchemaEntity schema) {

		log.debug("Creating collection '{}' for schema '{}'", schema.getMetadata().getCollection(), schemaUri);
		if (!mongoTemplate.collectionExists(schema.getMetadata().getNamespacedCollection())) {
			mongoTemplate.createCollection(schema.getMetadata().getNamespacedCollection());
		}
	}

	/**
	 * This method saves a json schema for document validation on the mongo collection
	 * starting from a joyce schema.
	 *
	 * @param schemaUri    Schema uri
	 * @param schema       Schema's body
	 * @param schemaObject Schema's body normalized for mongodb validation
	 */
	@Notify(
			successEvent = NotificationEvent.SINK_MONGODB_UPDATE_VALIDATION_SCHEMA_SUCCESS,
			failureEvent = NotificationEvent.SINK_MONGODB_UPDATE_VALIDATION_SCHEMA_FAILED
	)
	public void upsertCollectionValidator(
			@ContentUri String schemaUri,
			@EventPayload SchemaEntity schema,
			SchemaObject schemaObject) {

		if (schema.getMetadata().getValidation()) {
			log.debug("Updating validation schema for schema: '{}'", schemaUri);
			LinkedHashMap<String, Object> validatorCommand = new LinkedHashMap<>();
			validatorCommand.put("collMod", schema.getMetadata().getNamespacedCollection());
			validatorCommand.put("validator", this.computeValidationSchema(schemaObject));
			mongoTemplate.executeCommand(new Document(validatorCommand));
		}
	}

	/**
	 * This method automatically creates mongo indexes for schema fields and metadata fixed fields.
	 * An index is created only if it doesn't already exists.
	 * Schema field indexes that must be created are written in the schema metadata.
	 * Metadata field indexes that must be created are written in the application yaml.
	 *
	 * @param schemaUri Schema uri
	 * @param schema    Schema's body
	 */
	@Notify(
			successEvent = NotificationEvent.SINK_MONGODB_CREATE_INDEXES_SUCCESS,
			failureEvent = NotificationEvent.SINK_MONGODB_CREATE_INDEXES_FAILED
	)
	public void createIndexes(
			@ContentUri String schemaUri,
			@EventPayload SchemaEntity schema) {

		if (schema.getMetadata().getIndexed()) {
			log.debug("Creating indexes for schema: '{}'", schemaUri);
			List<Map<String, Object>> fieldIndexes = schema.getMetadata().getIndexes();
			this.insertIndexes(
					this.computeMongoIndexes(fieldIndexes),
					schema.getMetadata().getNamespacedCollection()
			);
		}
	}


	private Document computeValidationSchema(SchemaObject schemaObject) {
		return new Document(
				"$jsonSchema", mapper.convertValue(schemaObject, Document.class)
		);
	}


	/**
	 * This method aggregates field and metadata indexes and normalizes them for mongodb.
	 *
	 * @param fieldIndexes Field indexes from metadata
	 * @return Normalized Mongo indexes
	 */
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


	/**
	 * This method normalizes indexes for mongodb and generates names for them.
	 *
	 * @param indexes Mongo indexes
	 * @return Normalized mongo index
	 */
	private Stream<MongoIndex> buildMongoIndex(Map<String, Object> indexes) {
		return indexes.keySet().stream()
				.reduce((f1, f2) -> f1.concat("_").concat(f2))
				.filter(Predicate.not(String::isEmpty))
				.map(name -> MongoIndex.builder().name(name).fields(indexes).build())
				.stream();
	}

	/**
	 * This method checks if indexes are already present on a mongo collection
	 * confronting names and if they aren't it creates them.
	 *
	 * @param mongoIndexes Normalized mongo indexes
	 * @param collection   Mongo collection
	 */
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

	/**
	 * This method retrieves from a collection the names of existing indexes
	 *
	 * @param collection Mongo collection
	 * @return Existing indexes names
	 */
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
