package com.sourcesense.joyce.sink.mongodb.consumer;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sourcesense.joyce.core.enumeration.ImportAction;
import com.sourcesense.joyce.core.enumeration.KafkaCustomHeaders;
import com.sourcesense.joyce.core.exception.handler.CustomExceptionHandler;
import com.sourcesense.joyce.core.model.JoyceURI;
import com.sourcesense.joyce.sink.mongodb.service.SinkService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.Map;

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
	 * @param key     Document key
	 * @param headers Kafka headers
	 */
	@KafkaListener(topics = "${joyce.kafka.content.topic:joyce_content}")
	public void receive(
			@Payload ObjectNode message,
			@Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) String key,
			@Headers Map<String, String> headers) {

		try {

			String collection = sinkService.getCollection(key, headers);

			ImportAction action = ImportAction.valueOf(headers.getOrDefault(KafkaCustomHeaders.MESSAGE_ACTION, ImportAction.INSERT.name()));
			boolean storeContent = Boolean.parseBoolean(headers.getOrDefault(KafkaCustomHeaders.STORE_CONTENT, "true"));

			JoyceURI uri = sinkService.getJoyceURI(key);

			if (storeContent) {
				switch (action) {
					case INSERT:
						sinkService.saveDocument(message, uri, collection);
						break;
					case DELETE:
						sinkService.deleteDocument(uri, collection);
				}
			}
		} catch (Exception exception) {
			customExceptionHandler.handleException(exception);
		}
	}
}
