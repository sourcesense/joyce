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

package com.sourcesense.nile.importcore.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sourcesense.nile.core.dto.Schema;
import com.sourcesense.nile.core.enumeration.ImportAction;
import com.sourcesense.nile.core.enumeration.NotificationEvent;
import com.sourcesense.nile.core.exception.InvalidNileUriException;
import com.sourcesense.nile.core.exception.NotificationException;
import com.sourcesense.nile.core.exception.handler.CustomExceptionHandler;
import com.sourcesense.nile.core.model.NileURI;
import com.sourcesense.nile.core.service.SchemaService;
import com.sourcesense.nile.core.exception.SchemaNotFoundException;
import com.sourcesense.nile.core.enumeration.KafkaCustomHeaders;
import com.sourcesense.nile.importcore.dto.ConnectKeyPayload;
import com.sourcesense.nile.importcore.exception.ImportException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImportConsumer {
	final private ImportService importService;
	final private SchemaService schemaService;
	final private CustomExceptionHandler customExceptionHandler;
	final private ObjectMapper mapper;

	@KafkaListener(topics = "${nile.kafka.import-topic:import}")
	public void consumeMessage(@Payload ObjectNode message, @Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) String messageKey,
			@Headers Map<String, String> headers) {
		try {

			NileURI computedURI = computeUri(messageKey, headers);
			String computedMessageKey = computeMessageKey(messageKey, headers);
			Schema schema = computeSchema(computedURI, headers);
			processDocument(message, computedMessageKey, headers, schema);

		} catch (Exception e) {
			customExceptionHandler.handleNotificationException(new NotificationException(
					String.format("Cannot processImport message with key: %s error: %s", messageKey, e.getMessage()),
					NotificationEvent.RAW_DATA_IMPORT_FAILED));
		}
	}

	private NileURI computeUri(String messageKey, Map<String, String> headers) throws JsonProcessingException {

		Optional<NileURI> uri;

		// If we have the header we're receiving messages from a nile connector
		if (headers.get(KafkaCustomHeaders.IMPORT_SCHEMA) != null) {
			uri =  NileURI.createURI(headers.get(KafkaCustomHeaders.IMPORT_SCHEMA));

			// else We espect to have a key in json format in the format of ConnectKeyPayload and derive from that the
			// information we need.
			// It's the case of using plain kafka connect
		} else {
			ConnectKeyPayload key = mapper.readValue(messageKey, ConnectKeyPayload.class);
			checkValidKey(key);
			uri = NileURI.createURI(key.getSchema());
		}

		return uri.filter(nileURI -> nileURI.getSubtype().equals(NileURI.Subtype.IMPORT)).orElseThrow(
				() -> new InvalidNileUriException(
						String.format("Schema %s is not a valid schema uri", headers.get(KafkaCustomHeaders.IMPORT_SCHEMA))));

	}

	private String computeMessageKey(String messageKey, Map<String, String> headers) throws JsonProcessingException {
		if (headers.get(KafkaCustomHeaders.IMPORT_SCHEMA) == null) {

			ConnectKeyPayload key = mapper.readValue(messageKey, ConnectKeyPayload.class);
			checkValidKey(key);
			return NileURI.make(NileURI.Type.RAW, NileURI.Subtype.OTHER, key.getSource(), key.getUid()).toString();
		}

		return messageKey;

	}

	private Schema computeSchema(NileURI uri, Map<String, String> headers) {

		return schemaService.findByName(uri.getCollection())
				.orElseThrow(() -> new SchemaNotFoundException(String.format("Schema %s does not exists", uri.toString())));
	}

	private void processDocument(ObjectNode message, String messageKey, Map<String, String> headers, Schema schema) {
		//TODO: understand how to deal with deletion with kafka connect ingested content
		ImportAction action = ImportAction
				.valueOf(headers.getOrDefault(KafkaCustomHeaders.MESSAGE_ACTION, ImportAction.INSERT.name()));

		switch (action) {
		case DELETE:
			importService.removeDocument(schema, messageKey);
			break;
		case INSERT:
			importService.processImport(schema, message, messageKey);
			break;
		}
	}

	/**************** UTILITY METHODS *******************/

	private void checkValidKey(ConnectKeyPayload key) {
		if (key.getSchema() == null) {
			throw new ImportException("Missing [schema] from key");
		}
	}

}
