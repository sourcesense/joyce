package com.sourcesense.joyce.sink.mongodb.consumer;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sourcesense.joyce.core.enumeration.JoyceAction;
import com.sourcesense.joyce.core.exception.handler.CustomExceptionHandler;
import com.sourcesense.joyce.core.model.entity.JoyceKafkaKey;
import com.sourcesense.joyce.core.model.entity.JoyceKafkaKeyDefaultMetadata;
import com.sourcesense.joyce.core.model.uri.JoyceContentURI;
import com.sourcesense.joyce.sink.mongodb.service.SinkService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ContentConsumer {

	private final SinkService sinkService;
	private final CustomExceptionHandler customExceptionHandler;

	/**
	 * This method saves or removes from a mongo collection processed documents coming from kafka.
	 * Collection name and message key are retrieved from kafka headers.
	 * If the storeContent param retrieved from kafka headers is set to false, the insert/removal is skipped.
	 *
	 * @param message Processed document body
	 * @param messageKey Document key
	 */
	@KafkaListener(topics = "${joyce.kafka.content.topic:joyce_content}")
	public void receive(
			@Payload ObjectNode message,
			@Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) String messageKey){
		try {
			JoyceKafkaKey<JoyceContentURI, JoyceKafkaKeyDefaultMetadata> kafkaKey = sinkService.computeJoyceKafkaKey(messageKey);

			JoyceAction action = sinkService.computeAction(kafkaKey);
			boolean storeContent = sinkService.computeStoreContent(kafkaKey);
			String collection = sinkService.computeCollection(kafkaKey);

			if (storeContent) {
				switch (action) {
					case INSERT:
						sinkService.saveDocument(message, kafkaKey.getUri(), collection);
						break;
					case DELETE:
						sinkService.deleteDocument(kafkaKey.getUri(), collection);
				}
			}
		} catch (Exception exception) {
			customExceptionHandler.handleException(exception);
		}
	}
}
