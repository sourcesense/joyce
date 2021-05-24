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
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sourcesense.nile.core.annotation.EventPayload;
import com.sourcesense.nile.core.annotation.Notify;
import com.sourcesense.nile.core.annotation.RawUri;
import com.sourcesense.nile.core.dto.Schema;
import com.sourcesense.nile.core.enumeration.KafkaCustomHeaders;
import com.sourcesense.nile.core.enumeration.NotificationEvent;
import com.sourcesense.nile.core.exception.InvalidMetadataException;
import com.sourcesense.nile.core.exception.InvalidNileUriException;
import com.sourcesense.nile.core.exception.SchemaNotFoundException;
import com.sourcesense.nile.core.model.NileSchemaMetadata;
import com.sourcesense.nile.core.model.NileURI;
import com.sourcesense.nile.core.service.ContentProducer;
import com.sourcesense.nile.core.service.SchemaService;
import com.sourcesense.nile.importcore.dto.ConnectKeyPayload;
import com.sourcesense.nile.importcore.exception.ImportException;
import com.sourcesense.nile.schemaengine.exception.NileSchemaEngineException;
import com.sourcesense.nile.schemaengine.service.SchemaEngine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class ImportService {

	final private ObjectMapper mapper;
	final private SchemaEngine schemaEngine;
	final private SchemaService schemaService;
	final private ContentProducer contentProducer;

	@Notify(failureEvent = NotificationEvent.IMPORT_FAILED_INVALID_MESSAGE_KEY)
	public NileURI computeRawURI(
			@RawUri String messageKey,
			Map<String, String> headers) throws JsonProcessingException {

		if (headers.get(KafkaCustomHeaders.IMPORT_SCHEMA) == null) {

			ConnectKeyPayload key = mapper.readValue(messageKey, ConnectKeyPayload.class);
			checkValidKey(key);
			return NileURI.make(NileURI.Type.RAW, NileURI.Subtype.OTHER, key.getSource(), key.getUid());
		}

		return NileURI.createURI(messageKey)
				.orElseThrow(() -> new InvalidNileUriException(String.format("Uri [%s] is not a valid Nile Uri", messageKey)));
	}

	/**
	 * @param messageKey the key associated with the consumed kafka message
	 * @param headers    headers associated with the consumed kafka message. Obtaining the schema value from the headers
	 *                   will be suppressed in future releases and only the message key value will be used to calculate
	 *                   the NileURI
	 * @return Returns a NileURI calculated starting from the schema value present in the message key or in the headers.
	 * @throws JsonProcessingException
	 */
	@Notify(failureEvent = NotificationEvent.IMPORT_FAILED_INVALID_SCHEMA)
	public NileURI computeValidSchemaUri(
			String messageKey,
			Map<String, String> headers,
			@RawUri NileURI rawUri) throws JsonProcessingException {

		return this.computeSchemaUri(messageKey, headers)
				.filter(nileURI -> NileURI.Subtype.IMPORT.equals(nileURI.getSubtype()))
				.orElseThrow(
						() -> new InvalidNileUriException(
								String.format("Schema %s is not a valid schema uri", headers.get(KafkaCustomHeaders.IMPORT_SCHEMA))
						)
				);
	}

	@Notify(failureEvent = NotificationEvent.IMPORT_FAILED_INVALID_SCHEMA)
	public Schema computeSchema(@RawUri NileURI uri) {

		return schemaService.findByName(uri.getCollection())
				.orElseThrow(
						() -> new SchemaNotFoundException(
								String.format("Schema %s does not exists", uri.toString())
						)
				);
	}

	/**
	 * Process a document with the schema specified and processImport it on nile_content topic
	 *
	 * @param schema
	 * @param document
	 * @param rawUri
	 * @return
	 */
	@Notify(failureEvent = NotificationEvent.IMPORT_INSERT_FAILED)
	public boolean processImport(
			@RawUri NileURI rawUri,
			@EventPayload JsonNode document,
			Schema schema) {

		NileSchemaMetadata metadata = computeMetadata(schema);
		JsonNode result = schemaEngine.process(schema.getSchema(), document, null);

		computeParentMetadata(metadata, result, true).ifPresent(parentMetadata -> {
			metadata.setUidKey(parentMetadata.getUidKey());
			metadata.setCollection(parentMetadata.getCollection());
		});

		NileURI contentURI = computeContentURI(result, metadata);
		contentProducer.publishContent(schema, rawUri, contentURI, result, metadata);

		return true;
	}

	@Notify(failureEvent = NotificationEvent.IMPORT_REMOVE_FAILED)
	public void removeDocument(
			@RawUri NileURI rawUri,
			@EventPayload ObjectNode document,
			Schema schema) {

		NileSchemaMetadata metadata = computeMetadata(schema);

		JsonNode result = schemaEngine.process(schema.getSchema(), document, null);

		computeParentMetadata(metadata, result, false).ifPresent(parentMetadata -> {
			metadata.setUidKey(parentMetadata.getUidKey());
			metadata.setCollection(parentMetadata.getCollection());
		});

		NileURI contentURI = computeContentURI(result, metadata);

		contentProducer.removeContent(rawUri, contentURI, metadata);
	}

	/**
	 * Publish on nile_content topic the removal of a docuemnt
	 *
	 * @param schema
	 * @param rawUri
	 */
	@Notify(failureEvent = NotificationEvent.IMPORT_REMOVE_FAILED)
	public void removeDocument(
			@RawUri NileURI rawUri,
			Schema schema) {

		JsonNode metadataNode = schema.getSchema().get(SchemaEngine.METADATA);
		if (metadataNode == null) {
			throw new InvalidMetadataException("Schema has no metadata, cannot remove document");
		}

		NileSchemaMetadata metadata = mapper.convertValue(metadataNode, NileSchemaMetadata.class);
		contentProducer.removeContent(rawUri, metadata);
	}

	/**
	 * Processes a document with a schema returning it, without doing a real import
	 *
	 * @param schema
	 * @param document
	 * @return
	 */
	public JsonNode importDryRun(JsonNode document, Schema schema) {
		JsonNode result = schemaEngine.process(schema.getSchema(), document, null);
		return mapper.createObjectNode().putPOJO("result", result);
	}

	private Optional<NileURI> computeSchemaUri(String messageKey, Map<String, String> headers) throws JsonProcessingException {
		// If we have the header we're receiving messages from a nile connector
		if (headers.get(KafkaCustomHeaders.IMPORT_SCHEMA) != null) {
			return NileURI.createURI(headers.get(KafkaCustomHeaders.IMPORT_SCHEMA));

			// else We espect to have a key in json format in the format of ConnectKeyPayload and derive from that the
			// information we need.
			// It's the case of using plain kafka connect
		} else {
			ConnectKeyPayload key = mapper.readValue(messageKey, ConnectKeyPayload.class);
			checkValidKey(key);
			return NileURI.createURI(key.getSchema());
		}
	}

	private NileURI computeContentURI(JsonNode result, NileSchemaMetadata metadata) {
		String uid = Optional.ofNullable(result.get(metadata.getUidKey()))
				.orElseThrow(() -> new InvalidMetadataException(
						String.format("Missing [%s] key from document, cannot processImport document", metadata.getUidKey()))
				).asText();

		return NileURI.make(NileURI.Type.CONTENT, metadata.getSubtype(), metadata.getCollection(), uid);
	}

	private NileSchemaMetadata computeMetadata(Schema schema) {
		return Optional.ofNullable(schema)
				.map(Schema::getSchema)
				.map(s -> s.get(SchemaEngine.METADATA))
				.map(metadataNode -> mapper.convertValue(metadataNode, NileSchemaMetadata.class))
				.orElseThrow(() -> new InvalidMetadataException("Schema has no metadata"));
	}

	private Optional<NileSchemaMetadata> computeParentMetadata(NileSchemaMetadata metadata, JsonNode result, boolean validate) {
		/*
		 * If schema has a parent we validate with the parent
		 * and change some metadata with metadata from the parent
		 */
		if (metadata.getParent() != null) {

			Schema parent =  this.computeParentSchema(metadata);

			if (validate) {
				schemaEngine.validate(parent.getSchema(), result);
			}

			return Optional.of(parent)
					.map(Schema::getSchema)
					.map(schema -> schema.get(SchemaEngine.METADATA))
					.map(schemaMetadata -> mapper.convertValue(schemaMetadata, NileSchemaMetadata.class));
		}

		return Optional.empty();
	}

	private Schema computeParentSchema(NileSchemaMetadata metadata) {
		return Optional.of(metadata)
				.map(NileSchemaMetadata::getParent)
				.map(NileURI::toString)
				.flatMap(schemaService::get)
				.orElseThrow(() -> new NileSchemaEngineException(
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
