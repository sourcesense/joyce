package com.sourcesense.joyce.sink.mongodb.consumer;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sourcesense.joyce.core.enumeration.ImportAction;
import com.sourcesense.joyce.core.enumeration.KafkaCustomHeaders;
import com.sourcesense.joyce.core.exception.handler.CustomExceptionHandler;
import com.sourcesense.joyce.core.model.uri.JoyceDocumentURI;
import com.sourcesense.joyce.sink.mongodb.service.SinkService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

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
			// https://github.com/opentracing-contrib/java-kafka-client/issues/59
			String collection = headers.get(KafkaCustomHeaders.COLLECTION);
			ImportAction action = ImportAction.valueOf(headers.getOrDefault(KafkaCustomHeaders.MESSAGE_ACTION, ImportAction.INSERT.name()));
			boolean storeContent = Boolean.parseBoolean(headers.getOrDefault(KafkaCustomHeaders.STORE_CONTENT, "true"));

			if (storeContent) {
				switch (action) {
					case INSERT:
						sinkService.saveDocument(message, key, collection);
						break;
					case DELETE:
						sinkService.deleteDocument(key, collection);
				}
			}
		} catch (Exception exception) {
			customExceptionHandler.handleException(exception);
		}
	}
}
