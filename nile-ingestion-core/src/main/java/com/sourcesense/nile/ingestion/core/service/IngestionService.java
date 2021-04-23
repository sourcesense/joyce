package com.sourcesense.nile.ingestion.core.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sourcesense.nile.core.dto.Schema;
import com.sourcesense.nile.core.enumeration.NotificationEvent;
import com.sourcesense.nile.core.model.NileURI;
import com.sourcesense.nile.core.service.MainlogProducer;
import com.sourcesense.nile.core.service.NotificationService;
import com.sourcesense.nile.ingestion.core.errors.IngestionException;
import com.sourcesense.nile.schemaengine.dto.ProcessResult;
import com.sourcesense.nile.schemaengine.service.SchemaEngine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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

	public boolean ingest(Schema schema, JsonNode document) {
		try {
			ProcessResult result = schemaEngine.process(schema.getSchema(), document, null);
			NileURI uri = mainlogProducer.publishContent(schema, result);
			notificationEngine.ok(uri.toString(), NotificationEvent.INGESTION_SUCCEDED);
			return true;
		} catch (Exception e) {
			notificationEngine.ko(schema.getUid(), NotificationEvent.INGESTION_FAILED, e.getMessage());
			throw new IngestionException(e.getMessage());
		}

	}

	public void publishSchema(Schema saved) {
		try {
			mainlogProducer.publishSchema(saved);
			notificationEngine.ok(saved.getUid(), NotificationEvent.SCHEMA_PUBLISHED);
		} catch (Exception e) {
			notificationEngine.ko(saved.getUid(), NotificationEvent.SCHEMA_PUBLISH_FAILED, e.getMessage());
			throw new IngestionException(e.getMessage());
		}
	}
}
