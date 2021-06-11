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

package com.sourcesense.joyce.importcore.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sourcesense.joyce.core.annotation.EventPayload;
import com.sourcesense.joyce.core.annotation.Notify;
import com.sourcesense.joyce.core.annotation.RawUri;
import com.sourcesense.joyce.core.dto.Schema;
import com.sourcesense.joyce.core.enumeration.KafkaCustomHeaders;
import com.sourcesense.joyce.core.enumeration.NotificationEvent;
import com.sourcesense.joyce.core.exception.InvalidMetadataException;
import com.sourcesense.joyce.core.exception.InvalidJoyceUriException;
import com.sourcesense.joyce.core.exception.SchemaNotFoundException;
import com.sourcesense.joyce.core.model.JoyceSchemaMetadata;
import com.sourcesense.joyce.core.model.JoyceURI;
import com.sourcesense.joyce.core.service.ContentProducer;
import com.sourcesense.joyce.core.service.SchemaService;
import com.sourcesense.joyce.importcore.dto.ConnectKeyPayload;
import com.sourcesense.joyce.importcore.exception.ImportException;
import com.sourcesense.joyce.schemaengine.exception.JoyceSchemaEngineException;
import com.sourcesense.joyce.schemaengine.service.SchemaEngine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

/**
 * This class handles the business logic needed to process a raw message
 * until the message is send to kafka content topic.
 * It also sends notifications to kafka notification topic if
 * the message is sent successfully or if there is an error.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ImportService {

	final private ObjectMapper mapper;
	final private SchemaEngine schemaEngine;
	final private SchemaService schemaService;
	final private ContentProducer contentProducer;

	/**
	 *
	 * Builds an uri for the raw message used to identify the message.
	 * Mostly used to trace the message if there is an error.
	 *
	 * @param messageKey the key associated with the consumed kafka message
	 * @param headers  headers associated with the consumed kafka message. Obtaining the schema value from the headers
	 *                 will be suppressed in future releases and only the message key value will be used to calculate
	 *                 the JoyceURI
	 * @return The computed raw uri
	 * @throws JsonProcessingException
	 */
	@Notify(failureEvent = NotificationEvent.IMPORT_FAILED_INVALID_MESSAGE_KEY)
	public JoyceURI computeRawURI(
			@RawUri String messageKey,
			Map<String, String> headers) throws JsonProcessingException {

		if (headers.get(KafkaCustomHeaders.IMPORT_SCHEMA) == null) {

			ConnectKeyPayload key = mapper.readValue(messageKey, ConnectKeyPayload.class);
			checkValidKey(key);
			return JoyceURI.make(JoyceURI.Type.RAW, JoyceURI.Subtype.OTHER, key.getSource(), key.getUid());
		}

		return JoyceURI.createURI(messageKey)
				.orElseThrow(() -> new InvalidJoyceUriException(String.format("Uri [%s] is not a valid Joyce Uri", messageKey)));
	}

	/**
	 * Builds the uri of the schema that will be used to process the message
	 *
	 * @param messageKey the key associated with the consumed kafka message
	 * @param headers    headers associated with the consumed kafka message. Obtaining the schema value from the headers
	 *                   will be suppressed in future releases and only the message key value will be used to calculate
	 *                   the JoyceURI
	 * @param rawUri Uri used to trace the message if an error happens
	 * @return Uri calculated starting from the schema value present in the message key or in the headers.
	 * @throws JsonProcessingException
	 */
	@Notify(failureEvent = NotificationEvent.IMPORT_FAILED_INVALID_SCHEMA)
	public JoyceURI computeValidSchemaUri(
			String messageKey,
			Map<String, String> headers,
			@RawUri JoyceURI rawUri) throws JsonProcessingException {

		return this.computeSchemaUri(messageKey, headers)
				.filter(joyceURI -> JoyceURI.Subtype.IMPORT.equals(joyceURI.getSubtype()))
				.orElseThrow(
						() -> new InvalidJoyceUriException(
								String.format("Schema is not a valid schema uri. key: %s", messageKey))
						);
	}

	/**
	 * Uses the schema uri to fetch the schema on ksql.
	 *
	 * @param schemaUri Computed schema uri
	 * @param rawUri Uri used to trace the message if an error happens
	 * @return Schema used to process raw message
	 */
	@Notify(failureEvent = NotificationEvent.IMPORT_FAILED_INVALID_SCHEMA)
	public Schema computeSchema(
			JoyceURI schemaUri,
			@RawUri JoyceURI rawUri) {

		return schemaService.findByName(schemaUri.getCollection())
				.orElseThrow(
						() -> new SchemaNotFoundException(
								String.format("Schema %s does not exists", schemaUri.toString())
						)
				);
	}

	/**
	 * Process a document with the schema specified and processImport it on joyce_content topic
	 *
	 * @param rawUri Uri used to trace the message if an error happens
	 * @param document Raw message payload
	 * @param schema Schema used to process raw message
	 * @return true if the operation succeeded
	 */
	@Notify(failureEvent = NotificationEvent.IMPORT_INSERT_FAILED)
	public boolean processImport(
			@RawUri JoyceURI rawUri,
			@EventPayload JsonNode document,
			Schema schema) {

		JoyceSchemaMetadata metadata = computeMetadata(schema);
		JsonNode result = schemaEngine.process(schema.getSchema(), document, null);

		computeParentMetadata(metadata, result, true).ifPresent(parentMetadata -> {
			metadata.setUidKey(parentMetadata.getUidKey());
			metadata.setCollection(parentMetadata.getCollection());
		});

		JoyceURI contentURI = computeContentURI(result, metadata);
		contentProducer.publishContent(schema, rawUri, contentURI, result, metadata);

		return true;
	}

	/**
	 * Sends a message to kafka content topic that contains everything is needed to
	 * trigger document removal.
	 * Used by {@link com.sourcesense.joyce.importcore.controller.ImportController}
	 *
	 * @param rawUri Uri used to trace the message if an error happens
	 * @param document Raw message payload
	 * @param schema Schema used to process raw message
	 */
	@Notify(failureEvent = NotificationEvent.IMPORT_REMOVE_FAILED)
	public void removeDocument(
			@RawUri JoyceURI rawUri,
			@EventPayload ObjectNode document,
			Schema schema) {

		JoyceSchemaMetadata metadata = computeMetadata(schema);

		JsonNode result = schemaEngine.process(schema.getSchema(), document, null);

		computeParentMetadata(metadata, result, false).ifPresent(parentMetadata -> {
			metadata.setUidKey(parentMetadata.getUidKey());
			metadata.setCollection(parentMetadata.getCollection());
		});

		JoyceURI contentURI = computeContentURI(result, metadata);

		contentProducer.removeContent(rawUri, contentURI, metadata);
	}

	/**
	 * Sends a message to kafka content topic that contains everything is needed to
	 * trigger document removal.
	 * Used by {@link com.sourcesense.joyce.importcore.consumer.ImportConsumer}
	 *
	 * @param rawUri Uri used to trace the message if an error happens
	 * @param schema Schema used to process raw message
	 */
	@Notify(failureEvent = NotificationEvent.IMPORT_REMOVE_FAILED)
	public void removeDocument(
			@RawUri JoyceURI rawUri,
			Schema schema) {

		JsonNode metadataNode = schema.getSchema().get(SchemaEngine.METADATA);
		if (metadataNode == null) {
			throw new InvalidMetadataException("Schema has no metadata, cannot remove document");
		}

		JoyceSchemaMetadata metadata = mapper.convertValue(metadataNode, JoyceSchemaMetadata.class);
		contentProducer.removeContent(rawUri, metadata);
	}

	/**
	 * Processes a document with a schema returning it, without doing a real import
	 *
	 * @param document Raw message payload
	 * @param schema Schema used to process raw message
	 * @return Processed message
	 */
	public JsonNode importDryRun(JsonNode document, Schema schema) {
		JsonNode result = schemaEngine.process(schema.getSchema(), document, null);
		return mapper.createObjectNode().putPOJO("result", result);
	}

	/**
	 * Builds the schema uri in two different ways:
	 * 1)If import schema is in kafka headers we use that one. (joyce connector)
	 * 2)Else we will receive the key as a JsonNode (kafka connect)
	 *
	 * @param messageKey
	 * @param headers
	 * @return Optional Schema Uri
	 * @throws JsonProcessingException
	 */
	private Optional<JoyceURI> computeSchemaUri(String messageKey, Map<String, String> headers) throws JsonProcessingException {

		// If we have the header we're receiving messages from a joyce connector
		if (headers.get(KafkaCustomHeaders.IMPORT_SCHEMA) != null) {
			return JoyceURI.createURI(headers.get(KafkaCustomHeaders.IMPORT_SCHEMA));

			// else we expect to have a key in json format in the format of ConnectKeyPayload and derive from that the
			// information we need.
			// It's the case of using plain kafka connect
		} else {
			ConnectKeyPayload key = mapper.readValue(messageKey, ConnectKeyPayload.class);
			checkValidKey(key);
			return JoyceURI.createURI(key.getSchema());
		}
	}

	/**
	 * Build the content uri that will be used as key when
	 * we will send the processed message to kafka content topic
	 *
	 * @param result
	 * @param metadata
	 * @return Content uri
	 */
	private JoyceURI computeContentURI(JsonNode result, JoyceSchemaMetadata metadata) {
		String uid = Optional.ofNullable(result.get(metadata.getUidKey()))
				.orElseThrow(() -> new InvalidMetadataException(
						String.format("Missing [%s] key from document, cannot processImport document", metadata.getUidKey()))
				).asText();

		return JoyceURI.make(JoyceURI.Type.CONTENT, metadata.getSubtype(), metadata.getCollection(), uid);
	}

	/**
	 * Retrieves the metadata from the schema
	 *
	 * @param schema
	 * @return Schema metadata
	 */
	private JoyceSchemaMetadata computeMetadata(Schema schema) {
		return Optional.ofNullable(schema)
				.map(Schema::getSchema)
				.map(s -> s.get(SchemaEngine.METADATA))
				.map(metadataNode -> mapper.convertValue(metadataNode, JoyceSchemaMetadata.class))
				.orElseThrow(() -> new InvalidMetadataException("Schema has no metadata"));
	}

	/**
	 * Retrieves the parent metadata from the schema
	 *
	 * @param metadata
	 * @param result
	 * @param validate
	 * @return Optional schema parent metadata
	 */
	private Optional<JoyceSchemaMetadata> computeParentMetadata(JoyceSchemaMetadata metadata, JsonNode result, boolean validate) {
		/*
		 * If schema has a parent we validate with the parent
		 * and change some metadata with metadata from the parent
		 */
		if (metadata.getParent() != null) {

			Schema parent = this.computeParentSchema(metadata);

			if (validate) {
				schemaEngine.validate(parent.getSchema(), result);
			}

			return Optional.of(parent)
					.map(Schema::getSchema)
					.map(schema -> schema.get(SchemaEngine.METADATA))
					.map(schemaMetadata -> mapper.convertValue(schemaMetadata, JoyceSchemaMetadata.class));
		}

		return Optional.empty();
	}

	/**
	 * Retrieves the parent schema
	 *
	 * @param metadata
	 * @return Parent schema
	 */
	private Schema computeParentSchema(JoyceSchemaMetadata metadata) {
		return Optional.of(metadata)
				.map(JoyceSchemaMetadata::getParent)
				.map(JoyceURI::toString)
				.flatMap(schemaService::get)
				.orElseThrow(() -> new JoyceSchemaEngineException(
								String.format("Parent schema [%s] does not exists", metadata.getParent())
						)
				);
	}

	/**************** UTILITY METHODS *******************/

	private void checkValidKey(ConnectKeyPayload key) {

		if (StringUtils.isEmpty(key.getSchema())) {
			throw new ImportException("Missing [schema] from key");
		}
		if (StringUtils.isEmpty(key.getSource())) {
			throw new ImportException("Missing [source] from key");
		}
		if (StringUtils.isEmpty(key.getUid())) {
			throw new ImportException("Missing [uid] from key");
		}
	}

}
