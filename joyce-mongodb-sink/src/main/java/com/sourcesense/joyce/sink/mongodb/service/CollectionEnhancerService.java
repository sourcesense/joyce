package com.sourcesense.joyce.sink.mongodb.service;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.client.model.IndexOptions;
import com.sourcesense.joyce.core.annotation.ContentURI;
import com.sourcesense.joyce.core.annotation.EventPayload;
import com.sourcesense.joyce.core.annotation.Notify;
import com.sourcesense.joyce.core.configuration.mongo.MongodbProperties;
import com.sourcesense.joyce.core.enumeration.NotificationEvent;
import com.sourcesense.joyce.core.model.entity.JoyceKafkaKey;
import com.sourcesense.joyce.core.model.entity.JoyceKafkaKeyDefaultMetadata;
import com.sourcesense.joyce.core.model.entity.SchemaEntity;
import com.sourcesense.joyce.core.model.uri.JoyceSchemaURI;
import com.sourcesense.joyce.core.service.ConsumerService;
import com.sourcesense.joyce.core.utililty.SchemaUtils;
import com.sourcesense.joyce.sink.mongodb.exception.MongodbSinkException;
import com.sourcesense.joyce.sink.mongodb.model.JsonSchemaEntry;
import com.sourcesense.joyce.sink.mongodb.model.MongoIndex;
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
public class CollectionEnhancerService extends ConsumerService {

	private final SchemaUtils schemaUtils;
	private final MongoTemplate mongoTemplate;
	private final MongodbProperties mongodbProperties;

	public CollectionEnhancerService(
			ObjectMapper jsonMapper,
			SchemaUtils schemaUtils,
			MongoTemplate mongoTemplate,
			MongodbProperties mongodbProperties) {

		super(jsonMapper);
		this.schemaUtils = schemaUtils;
		this.mongoTemplate = mongoTemplate;
		this.mongodbProperties = mongodbProperties;
	}

	@Notify(failureEvent = NotificationEvent.SINK_MONGODB_ERROR_INVALID_SCHEMA_MESSAGE_KEY)
	public JoyceKafkaKey<JoyceSchemaURI, JoyceKafkaKeyDefaultMetadata> computeJoyceKafkaKey(@ContentURI String messageKey) throws JsonProcessingException {
		return super.computeKafkaKey(messageKey, JoyceSchemaURI.class, JoyceKafkaKeyDefaultMetadata.class);
	}

	/**
	 * This method creates a mongodb collection for the schema if the collection
	 * doesn't exist
	 *
	 * @param schemaURI Schema uri
	 * @param jsonSchema Schema unparsed body
	 */
	@Notify(
			successEvent = NotificationEvent.SINK_MONGODB_CREATE_COLLECTION_SUCCESS,
			failureEvent = NotificationEvent.SINK_MONGODB_CREATE_COLLECTION_FAILED
	)
	public void createCollection(@EventPayload ObjectNode jsonSchema, @ContentURI JoyceSchemaURI schemaURI) {
		SchemaEntity schema = this.computeSchema(jsonSchema, schemaURI, SchemaEntity.class);
		this.initCollection(schema);

		this.createIndexes(schema);

		JsonSchemaEntry jsonSchemaEntry = this.computeSchema(jsonSchema, schemaURI, JsonSchemaEntry.class);
		this.upsertCollectionValidator(schema, jsonSchemaEntry);
	}

	@Notify(
			successEvent = NotificationEvent.SINK_MONGODB_DELETE_COLLECTION_SUCCESS,
			failureEvent = NotificationEvent.SINK_MONGODB_DELETE_COLLECTION_FAILED
	)
	public void dropCollection(@ContentURI JoyceSchemaURI schemaURI) {
		mongoTemplate.dropCollection(schemaURI.getCollection());
	}

	/**
	 * This method reads a schema and converts it into a java object.
	 *
	 * @param schemaURI  Schema uri
	 * @param jsonSchema Schema's body
	 * @param clazz      Class object of the output java object
	 * @param <T>        Class of the output java object
	 * @return Converted schema
	 */
	private  <T> T computeSchema(JsonNode jsonSchema, JoyceSchemaURI schemaURI, Class<T> clazz) {
		log.debug("Parsing schema retrieved from kafka message");
		return schemaUtils.jsonToObject(jsonSchema, clazz)
				.orElseThrow(() -> new MongodbSinkException(String.format(
						"Impossible to parse schema from kafka message. Schema uri is '%s'", schemaURI
				)));
	}

	private void initCollection(SchemaEntity schema) {
		log.debug("Creating collection '{}' for schema '{}'", schema.getUid().getCollection(), schema.getUid());
		if (!mongoTemplate.collectionExists(schema.getUid().getCollection())) {
			mongoTemplate.createCollection(schema.getUid().getCollection());
		}
	}

	/**
	 * This method saves a json schema for document validation on the mongo collection
	 * starting from a joyce schema.
	 *
	 * @param schema          Schema's body
	 * @param jsonSchemaEntry Schema's body normalized for mongodb validation
	 */
	private void upsertCollectionValidator(
			SchemaEntity schema,
			JsonSchemaEntry jsonSchemaEntry) {

		if (schema.getMetadata().getValidation()) {
			log.debug("Updating validation schema for schema: '{}'", schema.getUid());
			LinkedHashMap<String, Object> validatorCommand = new LinkedHashMap<>();
			validatorCommand.put("collMod", schema.getUid().getCollection());
			validatorCommand.put("validator", this.computeValidationSchema(jsonSchemaEntry));
			mongoTemplate.executeCommand(new Document(validatorCommand));
		}
	}

	/**
	 * This method automatically creates mongo indexes for schema fields and metadata fixed fields.
	 * An index is created only if it doesn't already exists.
	 * Schema field indexes that must be created are written in the schema metadata.
	 * Metadata field indexes that must be created are written in the application yaml.
	 *
	 * @param schema    Schema's body
	 */
	private void createIndexes(SchemaEntity schema) {
		if (schema.getMetadata().getIndexed()) {
			log.debug("Creating indexes for schema: '{}'", schema.getUid());
			List<Map<String, Object>> fieldIndexes = schema.getMetadata().getIndexes();
			this.insertIndexes(
					this.computeMongoIndexes(fieldIndexes),
					schema.getUid().getCollection()
			);
		}
	}

	private Document computeValidationSchema(JsonSchemaEntry jsonSchemaEntry) {
		return new Document(
				"$jsonSchema", jsonMapper.convertValue(jsonSchemaEntry, Document.class)
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
						this.getMongoIndexesOrElseEmptyList(mongodbProperties.getMetadataIndexes()),
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
