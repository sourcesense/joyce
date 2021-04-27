package com.sourcesense.nile.ingestion.core.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sourcesense.nile.core.dto.Schema;
import com.sourcesense.nile.core.enumeration.NotificationEvent;
import com.sourcesense.nile.core.errors.InvalidMetadataException;
import com.sourcesense.nile.core.errors.InvalidNileUriException;
import com.sourcesense.nile.core.model.NileSchemaMetadata;
import com.sourcesense.nile.core.model.NileURI;
import com.sourcesense.nile.core.service.MainlogProducer;
import com.sourcesense.nile.core.service.NotificationService;
import com.sourcesense.nile.ingestion.core.errors.IngestionException;
import com.sourcesense.nile.schemaengine.dto.ProcessResult;
import com.sourcesense.nile.schemaengine.service.SchemaEngine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class IngestionService {

	final private SchemaEngine schemaEngine;
	final private ObjectMapper mapper;
	final private NotificationService notificationEngine;
	final private MainlogProducer mainlogProducer;

	public JsonNode ingestDryRun(Schema schema, JsonNode document) {
		ProcessResult node = schemaEngine.process(schema.getSchema(), document, null);

		ObjectNode result = mapper.createObjectNode();
		node.getMetadata().ifPresent(metadata -> {
			result.set("metadata", metadata);
		});
		result.set("result", node.getJson());

		return result;
	}

	public boolean ingest(Schema schema, JsonNode document, String rawUri) {
		try {

			ProcessResult result = schemaEngine.process(schema.getSchema(), document, null);

			if (result.getMetadata().isEmpty()){
				throw new InvalidMetadataException("Message has no metadata, cannot ingest document");
			}

			NileSchemaMetadata metadata = NileSchemaMetadata.create(result.getMetadata().get());
			String uid = Optional.ofNullable(result.getJson().get(metadata.getUidKey())).orElseThrow(() -> new InvalidMetadataException(String.format("Missing [%s] key from document, cannot ingest document", metadata.getUidKey()))).asText();

			NileURI key = NileURI.make(NileURI.Type.CONTENT, metadata.getSubtype(), metadata.getCollection(), uid);

			NileURI uri = mainlogProducer.publishContent(schema, result.getMetadata().get(), key, result.getJson(), rawUri);
			notificationEngine.ok(uri.toString(), NotificationEvent.MAINLOG_PUBLISH_SUCCESS);
			return true;
		} catch (Exception e) {
			notificationEngine.ko(schema.getUid(), NotificationEvent.MAINLOG_PUBLISH_FAILED, e.getMessage());
			throw new IngestionException(e.getMessage());
		}

	}

	public void publishSchema(Schema saved) {
		try {
			mainlogProducer.publishSchema(saved);
			notificationEngine.ok(saved.getUid(), NotificationEvent.SCHEMA_PUBLISH_SUCCESS);
		} catch (Exception e) {
			notificationEngine.ko(saved.getUid(), NotificationEvent.SCHEMA_PUBLISH_FAILED, e.getMessage());
			throw new IngestionException(e.getMessage());
		}
	}

	public void removeDocument(Schema schema, ObjectNode document) {
		ProcessResult result = schemaEngine.process(schema.getSchema(), document, null);

		if (result.getMetadata().isEmpty()){
			throw new InvalidMetadataException("Message has no metadata, cannot remove document");
		}

		NileSchemaMetadata metadata = NileSchemaMetadata.create(result.getMetadata().get());
		String uid = Optional.ofNullable(result.getJson().get(metadata.getUidKey())).orElseThrow(() -> new InvalidMetadataException(String.format("Missing [%s] key from document, cannot ingest document", metadata.getUidKey()))).asText();

		NileURI uri = NileURI.make(NileURI.Type.CONTENT, metadata.getSubtype(), metadata.getCollection(), uid);
		mainlogProducer.removeContent(result.getMetadata().get(), uri);
	}

	public void removeDocument(Schema schema, String rawUri) {
		JsonNode metadata = schema.getSchema().get(SchemaEngine.METADATA);
		if (metadata == null){
			throw new InvalidMetadataException("Schema has no metadata, cannot remove document");
		}
		Optional<NileURI> uri = NileURI.createURI(rawUri);
		if(uri.isEmpty()){
			throw new InvalidNileUriException(String.format("Uri [%s] is not a valid Nile Uri", rawUri));
		}
		mainlogProducer.removeContent(metadata, uri.get());
	}
}
