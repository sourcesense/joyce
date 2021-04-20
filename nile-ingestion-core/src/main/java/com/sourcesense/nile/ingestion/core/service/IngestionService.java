package com.sourcesense.nile.ingestion.core.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sourcesense.nile.core.dto.Schema;
import com.sourcesense.nile.core.model.NileURI;
import com.sourcesense.nile.core.service.MainlogProducer;
import com.sourcesense.nile.core.service.NotificationService;
import com.sourcesense.nile.ingestion.core.errors.IngestionException;
import com.sourcesense.nile.schemaengine.dto.ProcessResult;
import com.sourcesense.nile.schemaengine.service.SchemaEngine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

enum IngestionEvents {
	INGESTION_SUCCEDED,
	INGESTION_FAILED,
	SCHEMA_PUBLISHED,
	SCHEMA_PUBLISH_FAILED;

}

@Service
@Slf4j
@RequiredArgsConstructor
public class IngestionService {

	final private SchemaEngine schemaEngine;
	final private ObjectMapper mapper;
	final private NotificationService notificationEngine;
	final private MainlogProducer mainlogProducer;

	public JsonNode ingestDryRun(Schema schema, JsonNode document) {
		ProcessResult node = schemaEngine.process(schema.getSchema(), document);

		ObjectNode result = mapper.createObjectNode();
		node.getMetadata().ifPresent(metadata -> {
			result.set("metadata", metadata);
		});
		result.set("result", node.getJson());

		return result;
	}

	public boolean ingest(Schema schema, ObjectNode document) {
		try {
			ProcessResult result = schemaEngine.process(schema.getSchema(), document);
			NileURI uri = mainlogProducer.publishContent(schema, result);
			notificationEngine.ok(uri.toString(), IngestionEvents.INGESTION_SUCCEDED.toString());
			return true;
		} catch (Exception e) {
			notificationEngine.ko(schema.getUid(), IngestionEvents.INGESTION_FAILED.toString(), e.getMessage());
			throw new IngestionException(e.getMessage());
		}

	}

	public void publishSchema(Schema saved) {
		try {
			mainlogProducer.publishSchema(saved);
			notificationEngine.ok(saved.getUid(), IngestionEvents.SCHEMA_PUBLISHED.toString());
		} catch (Exception e) {
			notificationEngine.ko(saved.getUid(), IngestionEvents.SCHEMA_PUBLISH_FAILED.toString(), e.getMessage());
			throw new IngestionException(e.getMessage());
		}
	}
}
