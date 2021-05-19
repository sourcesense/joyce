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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sourcesense.nile.core.annotation.EventPayload;
import com.sourcesense.nile.core.annotation.Notify;
import com.sourcesense.nile.core.annotation.RawUri;
import com.sourcesense.nile.core.dto.Schema;
import com.sourcesense.nile.core.enumeration.NotificationEvent;
import com.sourcesense.nile.core.exception.InvalidMetadataException;
import com.sourcesense.nile.core.model.NileSchemaMetadata;
import com.sourcesense.nile.core.model.NileURI;
import com.sourcesense.nile.core.service.MainlogProducer;
import com.sourcesense.nile.core.service.NotificationService;
import com.sourcesense.nile.core.service.SchemaService;
import com.sourcesense.nile.schemaengine.exceptions.InvalidSchemaException;
import com.sourcesense.nile.schemaengine.service.SchemaEngine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class ImportService {

	final private ObjectMapper mapper;
	final private SchemaEngine schemaEngine;
	final private SchemaService schemaService;
	final private MainlogProducer mainlogProducer;

	/**
	 * Process a document with the schema specified and processImport it on mainlog topic
	 *
	 * @param schema
	 * @param document
	 * @param rawUri
	 * @return
	 */
	@Notify(failureEvent = NotificationEvent.RAW_DATA_INSERT_FAILED)
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
		NileURI publishedContentURI = mainlogProducer.publishContent(schema, rawUri, contentURI, result, metadata);

		return true;
	}

	@Notify(failureEvent = NotificationEvent.RAW_DATA_REMOVAL_FAILED)
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

		mainlogProducer.removeContent(rawUri, contentURI, metadata);
	}

	/**
	 * Publish on mainlog the removal of a docuemnt
	 *
	 * @param schema
	 * @param rawUri
	 */
	@Notify(failureEvent = NotificationEvent.RAW_DATA_REMOVAL_FAILED)
	public void removeDocument(
			@RawUri NileURI rawUri,
			Schema schema) {

		JsonNode metadataNode = schema.getSchema().get(SchemaEngine.METADATA);
		if (metadataNode == null) {
			throw new InvalidMetadataException("Schema has no metadata, cannot remove document");
		}

		NileSchemaMetadata metadata = mapper.convertValue(metadataNode, NileSchemaMetadata.class);
		mainlogProducer.removeContent(rawUri, metadata);
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

			Schema parent = Optional.of(metadata)
					.map(NileSchemaMetadata::getParent)
					.map(NileURI::toString)
					.flatMap(schemaService::get)
					.orElseThrow(() -> new InvalidSchemaException(
									String.format("Parent schema [%s] does not exists", metadata.getParent())
							)
					);

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
}
