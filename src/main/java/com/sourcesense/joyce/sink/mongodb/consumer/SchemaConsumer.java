package com.sourcesense.joyce.sink.mongodb.consumer;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sourcesense.joyce.core.exception.handler.CustomExceptionHandler;
import com.sourcesense.joyce.core.model.SchemaEntity;
import com.sourcesense.joyce.sink.mongodb.model.SchemaObject;
import com.sourcesense.joyce.sink.mongodb.service.CollectionEnhancer;
import io.netty.util.internal.StringUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Slf4j
@Component
@RequiredArgsConstructor
public class SchemaConsumer {

	private final CollectionEnhancer collectionEnhancer;
	private final CustomExceptionHandler customExceptionHandler;

	@KafkaListener(topics = "${joyce.schema-service.topic:joyce_schema}")
	public void receive(@Payload ObjectNode jsonSchema) {
		try {
			SchemaEntity schema = collectionEnhancer.computeSchema(StringUtil.EMPTY_STRING, jsonSchema, SchemaEntity.class);
			SchemaObject schemaObject = collectionEnhancer.computeSchema(schema.getUid(), jsonSchema, SchemaObject.class);

			String schemaCollection = schema.getMetadata().getCollection();
			collectionEnhancer.initCollection(schema.getUid(), schema);
			collectionEnhancer.upsertCollectionValidator(schema.getUid(), schemaObject, schemaCollection);
			collectionEnhancer.createIndexes(schema.getUid(), schema);

		} catch (Exception exception) {
			customExceptionHandler.handleException(exception);
		}
	}
}
