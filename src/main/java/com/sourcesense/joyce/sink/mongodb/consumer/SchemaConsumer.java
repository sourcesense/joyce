package com.sourcesense.joyce.sink.mongodb.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sourcesense.joyce.core.dao.utility.SchemaDaoUtil;
import com.sourcesense.joyce.core.exception.handler.CustomExceptionHandler;
import com.sourcesense.joyce.core.model.SchemaEntity;
import com.sourcesense.joyce.sink.mongodb.exception.MongodbSinkException;
import com.sourcesense.joyce.sink.mongodb.service.CollectionEnhancer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SchemaConsumer {

	private final SchemaDaoUtil schemaDaoUtil;
	private final CollectionEnhancer collectionEnhancer;
	private final CustomExceptionHandler customExceptionHandler;

	@KafkaListener(topics = "${joyce.schema-service.topic:joyce_schema}")
	public void receive(@Payload ObjectNode jsonSchema) {
		try {
			SchemaEntity schema = this.computeSchema(jsonSchema);
			collectionEnhancer.initCollection(schema.getUid(), schema);
			collectionEnhancer.upsertCollectionValidator(schema.getUid(), schema);
			collectionEnhancer.createIndexes(schema.getUid(), schema);

		} catch (Exception exception) {
			customExceptionHandler.handleException(exception);
		}
	}

	private SchemaEntity computeSchema(JsonNode jsonSchema) {
		return schemaDaoUtil.mapFromData(jsonSchema)
				.orElseThrow(() -> new MongodbSinkException(
						"Impossible to parse schema from kafka message")
				);
	}
}
