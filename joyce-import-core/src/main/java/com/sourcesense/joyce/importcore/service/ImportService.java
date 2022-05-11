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
import com.sourcesense.joyce.core.annotation.SourceURI;
import com.sourcesense.joyce.core.enumeration.FileExtension;
import com.sourcesense.joyce.core.enumeration.NotificationEvent;
import com.sourcesense.joyce.core.exception.InvalidMetadataException;
import com.sourcesense.joyce.core.model.entity.*;
import com.sourcesense.joyce.core.model.uri.*;
import com.sourcesense.joyce.core.producer.ContentProducer;
import com.sourcesense.joyce.core.service.ConsumerService;
import com.sourcesense.joyce.core.service.CsvMappingService;
import com.sourcesense.joyce.core.utililty.SchemaUtils;
import com.sourcesense.joyce.importcore.dto.BulkImportResult;
import com.sourcesense.joyce.importcore.dto.SingleImportResult;
import com.sourcesense.joyce.importcore.enumeration.ProcessStatus;
import com.sourcesense.joyce.importcore.exception.ImportException;
import com.sourcesense.joyce.schemacore.service.SchemaService;
import com.sourcesense.joyce.schemaengine.exception.JoyceSchemaEngineException;
import com.sourcesense.joyce.schemaengine.service.SchemaEngine;
import io.opentracing.Span;
import io.opentracing.util.GlobalTracer;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
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
public class ImportService extends ConsumerService {

	private final SchemaUtils schemaUtils;
	private final SchemaService schemaService;
	private final ContentProducer contentProducer;
	private final JsonLogicService jsonLogicService;
	private final CsvMappingService csvMappingService;
	private final SchemaEngine<SchemaEntity> schemaEngine;

	public ImportService(
			ObjectMapper jsonMapper,
			SchemaUtils schemaUtils,
		  SchemaService schemaService,
		  ContentProducer contentProducer,
		  JsonLogicService jsonLogicService,
		  CsvMappingService csvMappingService,
		  SchemaEngine<SchemaEntity> schemaEngine) {

		super(jsonMapper);
		this.schemaUtils = schemaUtils;
		this.schemaService = schemaService;
		this.contentProducer = contentProducer;
		this.jsonLogicService = jsonLogicService;
		this.csvMappingService = csvMappingService;
		this.schemaEngine = schemaEngine;
	}

	/**
	 * Builds the uri of the source
	 *
	 * @param messageKey the key associated with the consumed kafka message
	 * @return Object containing the converted kafkaKey
	 */
	@Notify(failureEvent = NotificationEvent.IMPORT_ERROR_INVALID_MESSAGE_KEY)
	public JoyceKafkaKey<JoyceSourceURI, JoyceKafkaKeyDefaultMetadata> computeJoyceKafkaKey(@SourceURI String messageKey) throws JsonProcessingException {
		return super.computeKafkaKey(messageKey);
	}

	/**
	 * Uses the schema uri to fetch the schema.
	 *
	 * @param sourceURI Uri used to trace the message if an error happens
	 * @return Schema used to process raw message
	 */
	@Notify(failureEvent = NotificationEvent.IMPORT_ERROR_INVALID_SCHEMA)
	public SchemaEntity computeSchema(@SourceURI JoyceSourceURI sourceURI) {

		return schemaService.getOrElseThrow(
				sourceURI.getDomain(),
				sourceURI.getProduct(),
				sourceURI.getName()
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
			@SourceURI JoyceSourceURI sourceURI,
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

			JoyceDocumentURI documentURI = this.computeDocumentURI(result, metadata);
			span.finish();

			contentProducer.publish(schema, documentURI, sourceURI, result, metadata);
			return SingleImportResult.builder().uri(sourceURI).processStatus(ProcessStatus.IMPORTED).build();

		} else {
			log.info("Document with uri {} wasn't processed cause it didn't pass metadata filter.", sourceURI);
			return SingleImportResult.builder().uri(sourceURI).processStatus(ProcessStatus.SKIPPED).build();
		}
	}

	@Notify(successEvent = NotificationEvent.IMPORT_BULK_INSERT_FAILED_INVALID_FILE)
	public List<JsonNode> computeDocumentsFromFile(
			@SourceURI JoyceSourceURI sourceURI,
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
			@SourceURI JoyceSourceURI sourceURI,
			@EventPayload BulkImportResult report) {

		return report;
	}

	/**
	 * Sends a message to kafka content topic that contains everything is needed to
	 * trigger document removal.
	 *
	 * @param sourceURI Uri used to trace the message if an error happens
	 * @param document  Raw message payload
	 * @param schema    Schema used to process raw message
	 */
	@Notify(failureEvent = NotificationEvent.IMPORT_REMOVE_FAILED)
	public SingleImportResult removeDocument(
			@SourceURI JoyceSourceURI sourceURI,
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

		contentProducer.remove(documentURI, sourceURI, metadata);
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
	 * @param schema    Schema used to process raw message
	 */
	@Notify(failureEvent = NotificationEvent.IMPORT_REMOVE_FAILED)
	public void removeDocument(
			@SourceURI JoyceSourceURI sourceURI,
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
}
