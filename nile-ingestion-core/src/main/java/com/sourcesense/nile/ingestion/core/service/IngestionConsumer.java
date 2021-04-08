package com.sourcesense.nile.ingestion.core.service;

import com.sourcesense.nile.core.dto.Schema;
import com.sourcesense.nile.core.service.SchemaService;
import com.sourcesense.nile.core.errors.SchemaNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class IngestionConsumer {
	final private IngestionService ingestionService;
	final private SchemaService schemaService;
	@KafkaListener(topics = "${nile.ingestion.kafka.ingestion-topic:ingestion}")
	public void listenIngestion(@Payload Map message,
														 @Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) String messageKey) {
		try {
			Optional<Schema> schema = schemaService.findByName(messageKey);
			if(schema.isEmpty()){
				throw new SchemaNotFoundException(String.format("Schema %s does not exists", messageKey));
			}
			ingestionService.ingest(schema.get(), message);
		} catch (Exception e) {
			//TODO: forward event to notification engine
			log.error("Cannot ingest message with key: {} error: {}", messageKey, e.getMessage());
		}
	}
}
