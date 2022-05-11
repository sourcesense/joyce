package com.sourcesense.joyce.sink.mongodb.consumer;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sourcesense.joyce.core.enumeration.JoyceAction;
import com.sourcesense.joyce.core.exception.handler.CustomExceptionHandler;
import com.sourcesense.joyce.core.model.entity.JoyceKafkaKey;
import com.sourcesense.joyce.core.model.entity.JoyceKafkaKeyDefaultMetadata;
import com.sourcesense.joyce.core.model.uri.JoyceSchemaURI;
import com.sourcesense.joyce.sink.mongodb.service.CollectionEnhancerService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
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
	public void receive(
			@Payload ObjectNode jsonSchema,
			@Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) String messageKey) {
		try {
			JoyceKafkaKey<JoyceSchemaURI, JoyceKafkaKeyDefaultMetadata> kafkaKey = collectionEnhancerService.computeJoyceKafkaKey(messageKey);

			JoyceAction action = collectionEnhancerService.computeAction(kafkaKey);
			String collection = collectionEnhancerService.computeCollection(kafkaKey);

			if (JoyceAction.INSERT.equals(action)) {
				//Todo: Check this part
				collectionEnhancerService.createCollection(jsonSchema, kafkaKey.getUri());
			} else {
				//TODO: check what collection should be deleted (parent or son)
				collectionEnhancerService.dropCollection(kafkaKey.getUri(), collection);
			}
		} catch (Exception exception) {
			customExceptionHandler.handleException(exception);
		}
	}
}
