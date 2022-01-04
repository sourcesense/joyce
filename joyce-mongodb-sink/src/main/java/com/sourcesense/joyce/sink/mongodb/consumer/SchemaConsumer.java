package com.sourcesense.joyce.sink.mongodb.consumer;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sourcesense.joyce.core.exception.handler.CustomExceptionHandler;
import com.sourcesense.joyce.core.model.SchemaEntity;
import com.sourcesense.joyce.sink.mongodb.model.SchemaObject;
import com.sourcesense.joyce.sink.mongodb.service.CollectionEnhancer;
import io.netty.util.internal.StringUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class SchemaConsumer {

	private final CollectionEnhancer collectionEnhancer;
	private final CustomExceptionHandler customExceptionHandler;

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
			SchemaEntity schema = collectionEnhancer.computeSchema(StringUtil.EMPTY_STRING, jsonSchema, SchemaEntity.class);
			SchemaObject schemaObject = collectionEnhancer.computeSchema(schema.getUid(), jsonSchema, SchemaObject.class);

			collectionEnhancer.initCollection(schema.getUid(), schema);
			collectionEnhancer.upsertCollectionValidator(schema.getUid(), schema, schemaObject);
			collectionEnhancer.createIndexes(schema.getUid(), schema);

		} catch (Exception exception) {
			customExceptionHandler.handleException(exception);
		}
	}
}
