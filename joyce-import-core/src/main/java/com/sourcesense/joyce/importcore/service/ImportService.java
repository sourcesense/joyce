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
import com.sourcesense.joyce.core.annotation.SourceUri;
import com.sourcesense.joyce.core.enumeration.FileExtension;
import com.sourcesense.joyce.core.enumeration.KafkaCustomHeaders;
import com.sourcesense.joyce.core.enumeration.NotificationEvent;
import com.sourcesense.joyce.core.enumeration.uri.JoyceURIChannel;
import com.sourcesense.joyce.core.exception.InvalidMetadataException;
import com.sourcesense.joyce.core.model.entity.JoyceSchemaMetadata;
import com.sourcesense.joyce.core.model.entity.SchemaEntity;
import com.sourcesense.joyce.core.model.uri.JoyceDocumentURI;
import com.sourcesense.joyce.core.model.uri.JoyceSchemaURI;
import com.sourcesense.joyce.core.model.uri.JoyceSourceURI;
import com.sourcesense.joyce.core.model.uri.JoyceURIFactory;
import com.sourcesense.joyce.core.producer.ContentProducer;
import com.sourcesense.joyce.core.service.CsvMappingService;
import com.sourcesense.joyce.core.utililty.SchemaUtils;
import com.sourcesense.joyce.importcore.dto.BulkImportResult;
import com.sourcesense.joyce.importcore.dto.ConnectKeyPayload;
import com.sourcesense.joyce.importcore.dto.SingleImportResult;
import com.sourcesense.joyce.importcore.enumeration.ProcessStatus;
import com.sourcesense.joyce.importcore.exception.ImportException;
import com.sourcesense.joyce.schemacore.service.SchemaService;
import com.sourcesense.joyce.schemaengine.exception.JoyceSchemaEngineException;
import com.sourcesense.joyce.schemaengine.service.SchemaEngine;
import io.opentracing.Span;
import io.opentracing.util.GlobalTracer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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

	private final ObjectMapper jsonMapper;
	private final SchemaUtils schemaUtils;
	private final SchemaService schemaService;
	private final ContentProducer contentProducer;
	private final JsonLogicService jsonLogicService;
	private final CsvMappingService csvMappingService;
	private final SchemaEngine<SchemaEntity> schemaEngine;

	/**
	 * Builds an uri for the raw message used to identify the message.
	 * Mostly used to trace the message if there is an error.
	 *
	 * @param messageKey the key associated with the consumed kafka message
	 * @param headers    headers associated with the consumed kafka message. Obtaining the schema value from the headers
	 *                   will be suppressed in future releases and only the message key value will be used to calculate
	 *                   the JoyceURI
	 * @return The computed raw uri
	 * @throws JsonProcessingException parsing exception
	 */
	@Notify(failureEvent = NotificationEvent.IMPORT_FAILED_INVALID_MESSAGE_KEY)
	public JoyceSourceURI computeSourceURI(
			@SourceUri String messageKey,
			Map<String, String> headers) throws JsonProcessingException {

		if (headers.get(KafkaCustomHeaders.IMPORT_SCHEMA) == null) {
			ConnectKeyPayload key = jsonMapper.readValue(messageKey, ConnectKeyPayload.class);
			this.checkValidKey(key);
			return JoyceURIFactory.getInstance().createSourceURIOrElseThrow(
					key.getSchema().getDomain(),
					key.getSchema().getProduct(),
					key.getSchema().getName(),
					JoyceURIChannel.CONNECT,
					key.getOrigin(),
					key.getUid()
			);
		}
		return JoyceURIFactory.getInstance().createURIOrElseThrow(messageKey, JoyceSourceURI.class);
	}

	/**
	 * Builds the uri of the schema that will be used to process the message
	 * Builds the schema uri in two different ways:
	 * 1)If import schema is in kafka headers we use that one. (joyce connector)
	 * 2)Else we will receive the key as a JsonNode (kafka connect)
	 *
	 * @param messageKey the key associated with the consumed kafka message
	 * @param headers    headers associated with the consumed kafka message. Obtaining the schema value from the headers
	 *                   will be suppressed in future releases and only the message key value will be used to calculate
	 *                   the JoyceURI
	 * @param sourceURI  Uri used to trace the message if an error happens
	 * @return Uri calculated starting from the schema value present in the message key or in the headers.
	 * @throws JsonProcessingException parsing exception
	 */
	@Notify(failureEvent = NotificationEvent.IMPORT_FAILED_INVALID_SCHEMA)
	public JoyceSchemaURI computeSchemaURI(
			String messageKey,
			Map<String, String> headers,
			@SourceUri JoyceSourceURI sourceURI) throws JsonProcessingException {

		// If we have the header we're receiving messages from a joyce connector
		String stringURI = headers.get(KafkaCustomHeaders.IMPORT_SCHEMA);
		if (stringURI != null) {
			return JoyceURIFactory.getInstance().createURIOrElseThrow(stringURI, JoyceSchemaURI.class);

			// else we expect to have a key in json format in the format of ConnectKeyPayload and derive from that the
			// information we need.
			// It's the case of using plain kafka connect
		} else {
			ConnectKeyPayload key = jsonMapper.readValue(messageKey, ConnectKeyPayload.class);
			checkValidKey(key);
			return key.getSchema();
		}
	}

	/**
	 * Uses the schema uri to fetch the schema.
	 *
	 * @param schemaUri Computed schema uri
	 * @param sourceURI    Uri used to trace the message if an error happens
	 * @return Schema used to process raw message
	 */
	@Notify(failureEvent = NotificationEvent.IMPORT_FAILED_INVALID_SCHEMA)
	public SchemaEntity computeSchema(
			JoyceSchemaURI schemaUri,
			@SourceUri JoyceSourceURI sourceURI) {

		return schemaService.getOrElseThrow(
				schemaUri.getDomain(),
				schemaUri.getProduct(),
				schemaUri.getName()
		);
	}

	/**
	 * Process a document with the schema specified and processImport it on joyce_content topic
	 *
	 * @param sourceURI Uri used to trace the message if an error happens
	 * @param document  Raw message payload
	 * @param schema    Schema used to process raw message
	 * @return true if the operation succeeded
	 */
	@Notify(failureEvent = NotificationEvent.IMPORT_INSERT_FAILED)
	public SingleImportResult processImport(
			@SourceUri JoyceSourceURI sourceURI,
			@EventPayload JsonNode document,
			SchemaEntity schema) {

		JoyceSchemaMetadata metadata = schemaUtils.metadataFromSchemaOrElseThrow(schema);
		if (jsonLogicService.filter(document, metadata)) {

			Span span = GlobalTracer.get().buildSpan("process").start();
			span.setTag("uri", Objects.nonNull(sourceURI) ? sourceURI.toString() : StringUtils.EMPTY);

			JsonNode result = schemaEngine.process(schema, document, null);

			computeParentMetadata(metadata, result, true)
					.ifPresent(parentMetadata -> {
						metadata.setUidKey(parentMetadata.getUidKey());
						metadata.setCollection(parentMetadata.getCollection());
						metadata.setDomain(parentMetadata.getDomain());
						metadata.setProduct(parentMetadata.getProduct());
					});

			JoyceDocumentURI contentURI = this.computeDocumentURI(result, metadata);
			span.finish();

			contentProducer.publish(schema, sourceURI, contentURI, result, metadata);
			return SingleImportResult.builder().uri(sourceURI).processStatus(ProcessStatus.IMPORTED).build();

		} else {
			log.info("Document with uri {} wasn't processed cause it didn't pass metadata filter.", sourceURI);
			return SingleImportResult.builder().uri(sourceURI).processStatus(ProcessStatus.SKIPPED).build();
		}
	}

	@Notify(successEvent = NotificationEvent.IMPORT_BULK_INSERT_FAILED_INVALID_FILE)
	public List<JsonNode> computeDocumentsFromFile(
			@SourceUri JoyceSourceURI sourceURI,
			MultipartFile data,
			Character columnSeparator,
			String arraySeparator) throws IOException {

		FileExtension fileExtension = FileExtension.getFileExtensionFromName(data.getOriginalFilename());
		switch (fileExtension) {
			case CSV:
				return csvMappingService.convertCsvFileToDocuments(data, columnSeparator, arraySeparator);
			case XLS:
			case XLSX:
			default:
				throw new ImportException(
						String.format("Impossible to retrieve extension for '%s'.", data.getOriginalFilename())
				);
		}
	}

	@Notify(successEvent = NotificationEvent.IMPORT_BULK_INSERT_SUCCESS)
	public BulkImportResult notifyBulkImportSuccess(
			@SourceUri JoyceSourceURI sourceURI,
			@EventPayload BulkImportResult report) {

		return report;
	}

	/**
	 * Sends a message to kafka content topic that contains everything is needed to
	 * trigger document removal.
	 *
	 * @param sourceURI   Uri used to trace the message if an error happens
	 * @param document Raw message payload
	 * @param schema   Schema used to process raw message
	 */
	@Notify(failureEvent = NotificationEvent.IMPORT_REMOVE_FAILED)
	public SingleImportResult removeDocument(
			@SourceUri JoyceSourceURI sourceURI,
			@EventPayload ObjectNode document,
			SchemaEntity schema) {

		JoyceSchemaMetadata metadata = schemaUtils.metadataFromSchemaOrElseThrow(schema);
		JsonNode result = schemaEngine.process(schema, document, null);

		computeParentMetadata(metadata, result, false)
				.ifPresent(parentMetadata -> {
					metadata.setUidKey(parentMetadata.getUidKey());
					metadata.setCollection(parentMetadata.getCollection());
				});

		JoyceDocumentURI documentURI = computeDocumentURI(result, metadata);

		contentProducer.remove(sourceURI, documentURI, metadata);
		return SingleImportResult.builder()
				.uri(sourceURI)
				.processStatus(ProcessStatus.DELETED)
				.build();
	}

	/**
	 * Sends a message to kafka content topic that contains everything is needed to
	 * trigger document removal.
	 * Used by {@link com.sourcesense.joyce.importcore.consumer.ImportConsumer}
	 *
	 * @param sourceURI Uri used to trace the message if an error happens
	 * @param schema Schema used to process raw message
	 */
	@Notify(failureEvent = NotificationEvent.IMPORT_REMOVE_FAILED)
	public void removeDocument(
			@SourceUri JoyceSourceURI sourceURI,
			SchemaEntity schema) {

		if (Objects.isNull(schema.getMetadata())) {
			throw new InvalidMetadataException("Schema has no metadata, cannot remove document");
		}
		contentProducer.remove(sourceURI, schema.getMetadata());
	}

	/**
	 * Processes a document with a schema returning it, without doing a real import
	 *
	 * @param document Raw message payload
	 * @param schema   Schema used to process raw message
	 * @return Processed message
	 */
	public SingleImportResult importDryRun(
			JoyceSourceURI sourceURI,
			JsonNode document,
			SchemaEntity schema) {

		JoyceSchemaMetadata metadata = schemaUtils.metadataFromSchemaOrElseThrow(schema);
		if (jsonLogicService.filter(document, metadata)) {

			JsonNode result = schemaEngine.process(schema, document, null);
			return SingleImportResult.builder()
					.uri(sourceURI)
					.processStatus(ProcessStatus.IMPORTED)
					.result(result)
					.build();

		} else {
			log.info("Document with uri {} wasn't processed cause it didn't pass metadata filter.", sourceURI);
			return SingleImportResult.builder()
					.uri(sourceURI)
					.processStatus(ProcessStatus.SKIPPED)
					.build();
		}
	}

	/**
	 * Build the content uri that will be used as key when
	 * we will send the processed message to kafka content topic
	 *
	 * @param result   result
	 * @param metadata metadata
	 * @return Content uri
	 */
	private JoyceDocumentURI computeDocumentURI(JsonNode result, JoyceSchemaMetadata metadata) {
		String uid = Optional.ofNullable(metadata.getUidKey())
				.map(result::get)
				.orElseThrow(() -> new InvalidMetadataException(
						String.format("Missing [%s] key from document, cannot processImport document", metadata.getUidKey()))
				).asText();

		return JoyceURIFactory.getInstance().createDocumentURIOrElseThrow(
				metadata.getDomain(),
				metadata.getProduct(),
				metadata.getName(),
				uid
		);
	}

	/**
	 * Retrieves the parent metadata from the schema
	 *
	 * @param metadata metadata
	 * @param result   result
	 * @param validate validate
	 * @return Optional schema parent metadata
	 */
	private Optional<JoyceSchemaMetadata> computeParentMetadata(JoyceSchemaMetadata metadata, JsonNode result, boolean validate) {
		/*
		 * If schema has a parent we validate with the parent
		 * and change some metadata with metadata from the parent
		 */
		if (metadata.getParent() != null) {
			SchemaEntity parent = this.computeParentSchema(metadata);

			if (validate) {
				schemaEngine.validate(parent, result);
			}
			return Optional.of(parent).map(SchemaEntity::getMetadata);
		}
		return Optional.empty();
	}

	/**
	 * Retrieves the parent schema
	 *
	 * @param metadata metadata
	 * @return Parent schema
	 */
	private SchemaEntity computeParentSchema(JoyceSchemaMetadata metadata) {
		return Optional.of(metadata)
				.map(JoyceSchemaMetadata::getParent)
				.map(JoyceSchemaURI::toString)
				.flatMap(schemaService::get)
				.orElseThrow(() -> new JoyceSchemaEngineException(
						String.format("Parent schema [%s] does not exists", metadata.getParent())
				));
	}

	/**************** UTILITY METHODS *******************/

	private void checkValidKey(ConnectKeyPayload key) {
		if (Objects.isNull(key.getSchema())) {
			throw new ImportException("Missing [schema] from key");
		}
		if (StringUtils.isEmpty(key.getOrigin())) {
			throw new ImportException("Missing [source] from key");
		}
		if (StringUtils.isEmpty(key.getUid())) {
			throw new ImportException("Missing [uid] from key");
		}
	}
}
