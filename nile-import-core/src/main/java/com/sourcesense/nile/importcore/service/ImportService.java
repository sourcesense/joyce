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
import com.sourcesense.nile.core.dto.Schema;
import com.sourcesense.nile.core.enumeration.NotificationEvent;
import com.sourcesense.nile.core.exception.InvalidMetadataException;
import com.sourcesense.nile.core.exception.InvalidNileUriException;
import com.sourcesense.nile.core.model.NileSchemaMetadata;
import com.sourcesense.nile.core.model.NileURI;
import com.sourcesense.nile.core.service.MainlogProducer;
import com.sourcesense.nile.core.service.NotificationService;
import com.sourcesense.nile.core.service.SchemaService;
import com.sourcesense.nile.importcore.exception.ImportException;
import com.sourcesense.nile.schemaengine.dto.ProcessResult;
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

	final private SchemaEngine schemaEngine;
	final private SchemaService schemaService;
	final private ObjectMapper mapper;
	final private NotificationService notificationEngine;
	final private MainlogProducer mainlogProducer;

	/**
	 * Processes a document with a schema returning it, without doing a real import
	 * @param schema
	 * @param document
	 * @return
	 */
	public JsonNode importDryRun(Schema schema, JsonNode document) {
		ProcessResult node = schemaEngine.process(schema.getSchema(), document, null);

		ObjectNode result = mapper.createObjectNode();
		node.getMetadata().ifPresent(metadata -> {
			result.set("metadata", metadata);
		});
		result.set("result", node.getJson());

		return result;
	}


	private NileSchemaMetadata computeMetadata(Schema schema) {
		return Optional.ofNullable(schema)
				.map(Schema::getSchema)
				.map(s -> s.get(SchemaEngine.METADATA))
				.map(metadataNode -> mapper.convertValue(metadataNode, NileSchemaMetadata.class))
				.orElseThrow(() -> new InvalidMetadataException("Schema has no metadata"));
	}

	/**
	 * Process a docuemnt with the schema specified and processImport it on mainlog topic
	 * @param schema
	 * @param document
	 * @param rawUri
	 * @return
	 */
	public boolean processImport(Schema schema, JsonNode document, NileURI rawUri) {

			NileSchemaMetadata metadata = computeMetadata(schema);
			ProcessResult result = schemaEngine.process(schema.getSchema(), document, null);

			computeParentMetadata(metadata, result, true).ifPresent(parentMetadata ->{
				metadata.setUidKey(parentMetadata.getUidKey());
				metadata.setCollection(parentMetadata.getCollection());
			});

			NileURI contentURI = computeContentURI(metadata, result);

			NileURI publishedContentURI = mainlogProducer.publishContent(schema, metadata, contentURI, result.getJson(), rawUri);

			return true;


	}

	private NileURI computeContentURI(NileSchemaMetadata metadata, ProcessResult result) {
		String uid = Optional.ofNullable(result.getJson().get(metadata.getUidKey())).orElseThrow(
				() -> new InvalidMetadataException(
						String.format("Missing [%s] key from document, cannot processImport document", metadata.getUidKey())))
				.asText();

		return NileURI.make(NileURI.Type.CONTENT, metadata.getSubtype(), metadata.getCollection(), uid);
	}

	private Optional<NileSchemaMetadata> computeParentMetadata(NileSchemaMetadata metadata, ProcessResult result, boolean validate) {
		/*
		 * If schema has a parent we validate with the parent
		 * and change some metadata with metadata from the parent
		 */
		if (metadata.getParent() != null) {
			Optional<Schema> parent = schemaService.get(metadata.getParent().toString());
			if (parent.isEmpty()) {
				throw new InvalidSchemaException(String.format("Parent schema [%s] does not exists", metadata.getParent()));
			}

			if(validate){
				schemaEngine.validate(parent.get().getSchema(), result.getJson());
			}

			return Optional
					.of(mapper.convertValue(parent.get().getSchema().get(SchemaEngine.METADATA), NileSchemaMetadata.class));

		}

		return Optional.empty();
	}

	/**
	 * Publish a schema on mainlog
	 *
	 * @param saved
	 */
	public void publishSchema(Schema saved) {
		try {
			mainlogProducer.publishSchema(saved);
			notificationEngine.ok(saved.getUid(), NotificationEvent.SCHEMA_PUBLISH_SUCCESS);
		} catch (Exception e) {
			notificationEngine.ko(saved.getUid(), NotificationEvent.SCHEMA_PUBLISH_FAILED, e.getMessage());
			throw new ImportException(e.getMessage());
		}
	}

	public void removeDocument(Schema schema, ObjectNode document) {

		NileSchemaMetadata metadata = computeMetadata(schema);

		ProcessResult result = schemaEngine.process(schema.getSchema(), document, null);

		computeParentMetadata(metadata, result, false).ifPresent(parentMetadata ->{
			metadata.setUidKey(parentMetadata.getUidKey());
			metadata.setCollection(parentMetadata.getCollection());
		});

		NileURI contentURI = computeContentURI(metadata, result);
		mainlogProducer.removeContent(metadata, contentURI);
	}

	/**
	 * Publish on mainlog the removal of a docuemnt
	 * @param schema
	 * @param rawUri
	 */
	public void removeDocument(Schema schema, NileURI rawUri) {
		JsonNode metadataNode = schema.getSchema().get(SchemaEngine.METADATA);
		if (metadataNode == null){
			throw new InvalidMetadataException("Schema has no metadata, cannot remove document");
		}

		NileSchemaMetadata metadata = mapper.convertValue(metadataNode, NileSchemaMetadata.class);
		mainlogProducer.removeContent(metadata, rawUri);
	}
}
