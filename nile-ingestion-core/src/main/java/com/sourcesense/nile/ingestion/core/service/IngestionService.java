package com.sourcesense.nile.ingestion.core.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sourcesense.nile.ingestion.core.dto.Schema;
import com.sourcesense.nile.ingestion.core.errors.MissingMetadataException;
import com.sourcesense.nile.ingestion.core.errors.SchemaNotFoundException;
import com.sourcesense.nile.ingestion.core.dao.Dao;
import com.sourcesense.nile.ingestion.core.model.SchemaEntity;
import com.sourcesense.nile.schemaengine.dto.ProcessResult;
import com.sourcesense.nile.schemaengine.service.SchemaEngine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.support.SendResult;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class IngestionService {
	public static String MESSAGE_KEY = "message_key";

	final private SchemaEngine schemaEngine;
	final private SchemaService schemaService;
	final private KafkaTemplate<String, Map> kafkaTemplate;
	final private ObjectMapper mapper;

	@Value("${nile.ingestion.kafka.mainlog:mainlog}")
	String mainlogTopic;

	private ProcessResult getProcessResult(String schemaId, Map document) throws JsonProcessingException {
		Optional<Schema> schema = schemaService.findById(schemaId);
		if(schema.isEmpty()){
			throw new SchemaNotFoundException(String.format("Schema %s does not exists", schemaId));
		}
		return schemaEngine.process(schema.get().getSchema(), document);
	}

	public Map processSchema(String schema, Map document) throws JsonProcessingException {
		ProcessResult node = getProcessResult(schema, document);

		Map result = new HashMap<>();
		node.getMetadata().ifPresent(metadata -> {
			result.put("metadata", metadata.getAll());
		});
		result.put("result", node.getJson());

		return result;
	}


	public Boolean ingest(String schema, Map document) throws JsonProcessingException {
		ProcessResult result = getProcessResult(schema, document);
		if (result.getMetadata().isEmpty()){
			throw new MissingMetadataException("Message has no metadata, cannot ingest docuemnt");
		}
		String key = Optional.ofNullable((String)result.getMetadata().get().get(MESSAGE_KEY)).orElseThrow(() -> new MissingMetadataException(String.format("Missing [%s] from metadata, cannot ingest document", MESSAGE_KEY)));

		MessageBuilder<Map> message = MessageBuilder
				.withPayload(result.getJson())
				.setHeader(KafkaHeaders.TOPIC, mainlogTopic)
				.setHeader(KafkaHeaders.MESSAGE_KEY, key);

		for(String metadataKey : result.getMetadata().get().getAll().keySet()){
			if (metadataKey != MESSAGE_KEY){
				Object value = result.getMetadata().get().get(metadataKey);
				if(!value.getClass().equals(String.class)){
					value = mapper.writeValueAsString(value);
				}
				message.setHeader(String.format("X-Nile-%s", metadataKey), value);
			}
		}

		ListenableFuture<SendResult<String, Map>> future = kafkaTemplate.send(message.build());

		future.addCallback(stringMapSendResult -> {
			log.debug("Correctly sent message with key: {} to kafka");
			//TODO: integrate with Notification Service Events
		}, throwable -> {
			//TODO: integrate with Notification Service Events
			log.error("Unable to send message with key {} error: {}", key, throwable.getMessage());
		});
		return true;
	}
}
