package com.sourcesense.nile.ingestion.core.service;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sourcesense.nile.core.dto.Schema;
import com.sourcesense.nile.core.enumeration.IngestionAction;
import com.sourcesense.nile.core.exceptions.InvalidNileUriException;
import com.sourcesense.nile.core.model.NileURI;
import com.sourcesense.nile.core.service.SchemaService;
import com.sourcesense.nile.core.exceptions.SchemaNotFoundException;
import com.sourcesense.nile.core.enumeration.KafkaCustomHeaders;
import com.sourcesense.nile.ingestion.core.errors.IngestionException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Headers;
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
	@KafkaListener(topics = "${nile.kafka.ingestion-topic:ingestion}")
	public void listenIngestion(@Payload ObjectNode message,
														 @Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) String messageKey,
															@Headers Map<String, String> headers) {
		try {

			if(headers.get(KafkaCustomHeaders.INGESTION_SCHEMA) == null){
				throw new IngestionException(String.format("Missing %s header in message", KafkaCustomHeaders.INGESTION_SCHEMA));
			}
			Optional<NileURI> uri = NileURI.createURI(headers.get(KafkaCustomHeaders.INGESTION_SCHEMA));
			if (uri.isEmpty() || !uri.get().getSubtype().equals(NileURI.Subtype.IMPORT)){
				throw new InvalidNileUriException(String.format("Schema %s is not a valid schema uri", headers.get(KafkaCustomHeaders.INGESTION_SCHEMA)));
			}

			Optional<Schema> schema = schemaService.findByName(uri.get().getCollection());
			if(schema.isEmpty()){
				throw new SchemaNotFoundException(String.format("Schema %s does not exists", headers.get(KafkaCustomHeaders.INGESTION_SCHEMA)));
			}

			IngestionAction action = IngestionAction.valueOf(headers.getOrDefault(KafkaCustomHeaders.MESSAGE_ACTION, IngestionAction.INSERT.name()));
			switch (action){
				case DELETE:
					ingestionService.removeDocument(schema.get(), messageKey);
					break;
				case INSERT:
					ingestionService.ingest(schema.get(), message, messageKey);
					break;
			}


		} catch (Exception e) {
			//TODO: forward event to notification engine
			log.error("Cannot ingest message with key: {} error: {}", messageKey, e.getMessage());
		}
	}
}
