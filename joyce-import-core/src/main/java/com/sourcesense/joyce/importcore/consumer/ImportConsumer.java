/*
 * Copyright 2021 Sourcesense Spa
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sourcesense.joyce.importcore.consumer;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sourcesense.joyce.core.enumeration.ImportAction;
import com.sourcesense.joyce.core.enumeration.JoyceSchemaType;
import com.sourcesense.joyce.core.enumeration.KafkaCustomHeaders;
import com.sourcesense.joyce.core.exception.InvalidMetadataException;
import com.sourcesense.joyce.core.exception.handler.CustomExceptionHandler;
import com.sourcesense.joyce.core.model.entity.SchemaEntity;
import com.sourcesense.joyce.core.model.uri.JoyceSchemaURI;
import com.sourcesense.joyce.core.model.uri.JoyceSourceURI;
import com.sourcesense.joyce.importcore.service.ImportService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class ImportConsumer {

	final private ImportService importService;
	final private CustomExceptionHandler customExceptionHandler;

	/**
	 * Kafka consumer that reads raw messages from the import topic and processes them using
	 * a schema .
	 * There are two types of action that can be executed on a message: Insert and Delete.
	 *
	 * @param message The payload of the kafka message
	 * @param messageKey The key of the kafka message
	 * @param headers the headers of the kafka message
	 */
	@KafkaListener(topics = "${joyce.kafka.import.topic:joyce_import}")
	public void consumeMessage(
			@Payload ObjectNode message,
			@Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) String messageKey,
			@Headers Map<String, String> headers) {

		try {
			JoyceSourceURI sourceURI = importService.computeSourceURI(messageKey, headers);
			JoyceSchemaURI schemaURI = importService.computeSchemaURI(messageKey, headers, sourceURI);
			SchemaEntity schema = importService.computeSchema(schemaURI, sourceURI);

			if(!JoyceSchemaType.IMPORT.equalsIgnoreCase(schema.getMetadata().getType())) {
				throw new InvalidMetadataException(String.format(
						"Impossible to consume message %s, schema %s isn't an import scheme", sourceURI, schemaURI
				));
			}

			String action = headers.getOrDefault(KafkaCustomHeaders.MESSAGE_ACTION, ImportAction.INSERT.name());
			switch (ImportAction.valueOf(action)) {
				case DELETE:
					importService.removeDocument(sourceURI, schema);
					break;
				case INSERT:
					importService.processImport(sourceURI, message, schema);
					break;
			}
		} catch (Exception exception) {
			customExceptionHandler.handleException(exception);
		}
	}
}
