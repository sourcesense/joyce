package com.sourcesense.nile.ingestion.core.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sourcesense.nile.core.dto.Schema;
import com.sourcesense.nile.core.service.NotificationService;
import com.sourcesense.nile.ingestion.core.errors.MissingMetadataException;
import com.sourcesense.nile.schemaengine.dto.ProcessResult;
import com.sourcesense.nile.schemaengine.service.SchemaEngine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.support.SendResult;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;

import java.util.*;

enum IngestionEvents {
	INGESTION_SUCCEDED,
	INGESTION_FAILED,
	PROCESSING_FAILED;
}

@Service
@Slf4j
@RequiredArgsConstructor
public class IngestionService {
	public static String MESSAGE_KEY = "message_key";

	final private SchemaEngine schemaEngine;
	final private KafkaTemplate<String, JsonNode> kafkaTemplate;
	final private ObjectMapper mapper;
	final private NotificationService notificationEngine;

	@Value("${nile.kafka.mainlog-topic:mainlog}")
	String mainlogTopic;

	public JsonNode processSchema(Schema schema, JsonNode document) {
		ProcessResult node = schemaEngine.process(schema.getSchema(), document);

		// Set schema version
		setSchemaMetadata(schema, (ObjectNode) node.getJson());

		ObjectNode result = mapper.createObjectNode();
		node.getMetadata().ifPresent(metadata -> {
			result.set("metadata", metadata);
		});
		result.set("result", node.getJson());

		return result;
	}

	private void setSchemaMetadata(Schema schema, ObjectNode json) {
		json.put("_schema_version_", schema.getVersion());
		json.put("_schema_uid_", schema.getUid());

		json.put("_schema_name_", schema.getName());
	}


	public Boolean ingest(Schema schema, ObjectNode document) throws JsonProcessingException {
		ProcessResult result = schemaEngine.process(schema.getSchema(), document);

		if (result.getMetadata().isEmpty()){
			MissingMetadataException e = new MissingMetadataException("Message has no metadata, cannot ingest document");
			ObjectNode metadata = mapper.createObjectNode();
			metadata.put("error", e.getMessage());
			notificationEngine.ko(null, IngestionEvents.PROCESSING_FAILED.toString(), metadata);
			throw e;
		}

		String key = Optional.ofNullable(result.getMetadata().get().get(MESSAGE_KEY).asText()).orElseThrow(() -> {
			MissingMetadataException e = new MissingMetadataException(String.format("Missing [%s] from metadata, cannot ingest document", MESSAGE_KEY));
			ObjectNode metadata = mapper.createObjectNode();
			metadata.put("error", e.getMessage());
			notificationEngine.ko(null, IngestionEvents.PROCESSING_FAILED.toString(), metadata);
			throw e;
		});

		// Set schema version
		setSchemaMetadata(schema, (ObjectNode) result.getJson());

		MessageBuilder<JsonNode> message = MessageBuilder
				.withPayload(result.getJson())
				.setHeader(KafkaHeaders.TOPIC, mainlogTopic)
				.setHeader(KafkaHeaders.MESSAGE_KEY, key);

		for (Iterator<Map.Entry<String, JsonNode>> it = result.getMetadata().get().fields(); it.hasNext(); ) {
			Map.Entry<String, JsonNode> item = it.next();
			if (!item.getKey().equals(MESSAGE_KEY)){
				message.setHeader(String.format("X-Nile-%s", item.getKey()), item.getValue().asText());
			}
		}

		ListenableFuture<SendResult<String, JsonNode>> future = kafkaTemplate.send(message.build());

		future.addCallback(stringMapSendResult -> {
			log.debug("Correctly sent message with key: {} to kafka");

			notificationEngine.ok(key, IngestionEvents.INGESTION_SUCCEDED.toString(), null);
		}, throwable -> {
			ObjectNode metadata = mapper.createObjectNode();
			metadata.put("error", throwable.getMessage());
			notificationEngine.ko(key, IngestionEvents.INGESTION_FAILED.toString(), metadata);
			log.error("Unable to send message with key {} error: {}", key, throwable.getMessage());
		});
		return true;
	}
}
