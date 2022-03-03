package com.sourcesense.joyce.sink.mongodb.consumer;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sourcesense.joyce.core.enumeration.ImportAction;
import com.sourcesense.joyce.core.enumeration.KafkaCustomHeaders;
import com.sourcesense.joyce.core.exception.handler.CustomExceptionHandler;
import com.sourcesense.joyce.core.model.SchemaEntity;
import com.sourcesense.joyce.sink.mongodb.model.JsonSchemaEntry;
import com.sourcesense.joyce.sink.mongodb.service.CollectionEnhancerService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;


@Component
@RequiredArgsConstructor
public class SchemaConsumer {

	private final CustomExceptionHandler customExceptionHandler;
	private final CollectionEnhancerService collectionEnhancerService;
	private final MongoTemplate mongoTemplate;

	/**
	 * This method provides the following enhancement to the mongo collection for a certain schema:
	 * 1) Creates the collection if it doesn't already exists
	 * 2) Saves and Updates mongodb json schema validator created starting from the schema
	 * 3) Automatically creates indexes for schema field and metadata
	 *
	 * @param jsonSchema The starting json schema
	 */
	@KafkaListener(topics = "${joyce.kafka.schema.topic:joyce_schema}")
	public void receive(@Payload ObjectNode jsonSchema,
											@Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) String key,
											@Headers Map<String, String> headers) {
		try {
			ImportAction action = Optional.ofNullable(headers.get(KafkaCustomHeaders.MESSAGE_ACTION)).map(ImportAction::valueOf).orElse(ImportAction.INSERT);
			if (action.equals(ImportAction.INSERT)) {
				SchemaEntity schema = collectionEnhancerService.computeSchema(StringUtils.EMPTY, jsonSchema, SchemaEntity.class);
				JsonSchemaEntry jsonSchemaEntry = collectionEnhancerService.computeSchema(schema.getUid(), jsonSchema, JsonSchemaEntry.class);

				collectionEnhancerService.initCollection(schema.getUid(), schema);
				collectionEnhancerService.upsertCollectionValidator(schema.getUid(), schema, jsonSchemaEntry);
				collectionEnhancerService.createIndexes(schema.getUid(), schema);
			} else {
				// Delete
				Optional<String> collection = Optional.ofNullable(headers.get(KafkaCustomHeaders.COLLECTION));
				collection.ifPresent(coll -> {
					mongoTemplate.dropCollection(coll);
				});
			}

		} catch (Exception exception) {
			customExceptionHandler.handleException(exception);
		}
	}
}
