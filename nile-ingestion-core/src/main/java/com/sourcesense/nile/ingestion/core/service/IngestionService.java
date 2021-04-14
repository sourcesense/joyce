package com.sourcesense.nile.ingestion.core.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sourcesense.nile.core.NileConstant;
import com.sourcesense.nile.core.dto.Schema;
import com.sourcesense.nile.core.model.NileURI;
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

import java.util.Iterator;
import java.util.Map;
import java.util.Optional;

enum IngestionEvents {
	INGESTION_SUCCEDED,
	INGESTION_FAILED,
	PROCESSING_FAILED,
	SCHEMA_PUBLISHED,
	SCHEMA_PUBLISH_FAILED;

}

@Service
@Slf4j
@RequiredArgsConstructor
public class IngestionService {

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


		String collection = result.getMetadata().get().get(NileConstant.KEY_COLLECTION).asText();
		String uidKey = result.getMetadata().get().get(NileConstant.KEY_UID).asText();

		if(collection == null || uidKey == null){
			MissingMetadataException e = new MissingMetadataException("Missing uid or collection from metadata, cannot ingest document");

			ObjectNode metadata = mapper.createObjectNode();
			metadata.put("error", e.getMessage());
			notificationEngine.ko(null, IngestionEvents.PROCESSING_FAILED.toString(), metadata);
			throw e;
		}

		String uid = result.getJson().get(uidKey).asText();

		if(uid == null){
			MissingMetadataException e = new MissingMetadataException(String.format("Missing [%s] key from document, cannot ingest document", uidKey));

			ObjectNode metadata = mapper.createObjectNode();
			metadata.put("error", e.getMessage());
			notificationEngine.ko(null, IngestionEvents.PROCESSING_FAILED.toString(), metadata);
			throw e;
		}

		NileURI key = NileURI.make(NileURI.Type.CONTENT, NileURI.Subtype.IMPORT, collection, uid);

		// Set schema version
		setSchemaMetadata(schema, (ObjectNode) result.getJson());

		MessageBuilder<JsonNode> message = MessageBuilder
				.withPayload(result.getJson())
				.setHeader(KafkaHeaders.TOPIC, mainlogTopic)
				.setHeader(NileConstant.HEADER_SOURCE, NileConstant.INGESTION)
				.setHeader(KafkaHeaders.MESSAGE_KEY, key.toString());

		for (Iterator<Map.Entry<String, JsonNode>> it = result.getMetadata().get().fields(); it.hasNext(); ) {
			Map.Entry<String, JsonNode> item = it.next();
			message.setHeader(String.format("X-Nile-%s", item.getKey()), item.getValue().asText());
		}

		ListenableFuture<SendResult<String, JsonNode>> future = kafkaTemplate.send(message.build());

		future.addCallback(stringMapSendResult -> {
			log.debug("Correctly sent message with key: {} to kafka", key.toString());

			notificationEngine.ok(key.toString(), IngestionEvents.INGESTION_SUCCEDED.toString(), null);
		}, throwable -> {
			ObjectNode metadata = mapper.createObjectNode();
			metadata.put("error", throwable.getMessage());
			notificationEngine.ko(key.toString(), IngestionEvents.INGESTION_FAILED.toString(), metadata);
			log.error("Unable to send message with key {} error: {}", key, throwable.getMessage());
		});
		return true;
	}


	/**
	 * Publish Schema on main log topic
	 * @param schema
	 */
	public void publishSchema(Schema schema) {
		MessageBuilder<JsonNode> message = MessageBuilder
				.withPayload(schema.getSchema())
				.setHeader(KafkaHeaders.TOPIC, mainlogTopic)
				.setHeader(NileConstant.HEADER_SOURCE, NileConstant.INGESTION)
				.setHeader(KafkaHeaders.MESSAGE_KEY, schema.getUid());
		ListenableFuture<SendResult<String, JsonNode>> future = kafkaTemplate.send(message.build());

		future.addCallback(stringMapSendResult -> {
			log.debug("Correctly sent message with key: {} to kafka");

			notificationEngine.ok(schema.getUid(), IngestionEvents.SCHEMA_PUBLISHED.toString(), null);
		}, throwable -> {
			ObjectNode metadata = mapper.createObjectNode();
			metadata.put("error", throwable.getMessage());
			notificationEngine.ko(schema.getUid(), IngestionEvents.SCHEMA_PUBLISH_FAILED.toString(), metadata);
			log.error("Unable to send schema with key {} error: {}", schema.getUid(), throwable.getMessage());
		});
	}
}
