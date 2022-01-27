package com.sourcesense.joyce.sink.mongodb.consumer;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sourcesense.joyce.core.exception.handler.CustomExceptionHandler;
import com.sourcesense.joyce.core.model.SchemaEntity;
import com.sourcesense.joyce.sink.mongodb.model.SchemaObject;
import com.sourcesense.joyce.sink.mongodb.service.CollectionEnhancerService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class SchemaConsumer {

	private final CustomExceptionHandler customExceptionHandler;
	private final CollectionEnhancerService collectionEnhancerService;

	/**
	 * This method provides the following enhancement to the mongo collection for a certain schema:
	 * 1) Creates the collection if it doesn't already exists
	 * 2) Saves and Updates mongodb json schema validator created starting from the schema
	 * 3) Automatically creates indexes for schema field and metadata
	 *
	 * @param jsonSchema The starting json schema
	 */
	@KafkaListener(topics = "${joyce.kafka.schema.topic:joyce_schema}")
	public void receive(@Payload ObjectNode jsonSchema) {
		try {
			SchemaEntity schema = collectionEnhancerService.computeSchema(StringUtils.EMPTY, jsonSchema, SchemaEntity.class);
			SchemaObject schemaObject = collectionEnhancerService.computeSchema(schema.getUid(), jsonSchema, SchemaObject.class);

			collectionEnhancerService.initCollection(schema.getUid(), schema);
			collectionEnhancerService.upsertCollectionValidator(schema.getUid(), schema, schemaObject);
			collectionEnhancerService.createIndexes(schema.getUid(), schema);

		} catch (Exception exception) {
			customExceptionHandler.handleException(exception);
		}
	}
}
