package com.sourcesense.nile.core.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sourcesense.nile.core.dto.Schema;
import com.sourcesense.nile.core.errors.InvalidMetadataException;
import com.sourcesense.nile.core.model.NileSchemaMetadata;
import com.sourcesense.nile.core.model.NileURI;
import com.sourcesense.nile.schemaengine.dto.ProcessResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.support.SendResult;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;

import java.util.Iterator;
import java.util.Map;
import java.util.Optional;

@Slf4j
@ConditionalOnProperty(value = "nile.mainlog-producer.enabled", havingValue = "true")
@Service
@RequiredArgsConstructor
public class MainlogProducer {
    final private KafkaTemplate<String, JsonNode> kafkaTemplate;

    @Value("${nile.kafka.mainlog-topic:mainlog}")
    String mainlogTopic;

    public NileURI publishContent(Schema schema, ProcessResult result) {

        if (result.getMetadata().isEmpty()){
            throw new InvalidMetadataException("Message has no metadata, cannot ingest document");
        }

        NileSchemaMetadata metadata = NileSchemaMetadata.create(result.getMetadata().get());

        String uid = Optional.ofNullable(result.getJson().get(metadata.getUidKey())).orElseThrow(() -> new InvalidMetadataException(String.format("Missing [%s] key from document, cannot ingest document", metadata.getUidKey()))).asText();

        NileURI key = NileURI.make(NileURI.Type.CONTENT, metadata.getSubtype(), metadata.getCollection(), uid);

        // Set schema version
        ((ObjectNode) result.getJson()).put("_schema_version_", schema.getVersion());
        ((ObjectNode) result.getJson()).put("_schema_uid_", schema.getUid());
        ((ObjectNode) result.getJson()).put("_schema_name_", schema.getName());
        ((ObjectNode) result.getJson()).put("_schema_development_", schema.getDevelopment());

        MessageBuilder<JsonNode> message = MessageBuilder
                .withPayload(result.getJson())
                .setHeader(KafkaHeaders.TOPIC, mainlogTopic)
                .setHeader(KafkaHeaders.MESSAGE_KEY, key.toString());

        for (Iterator<Map.Entry<String, JsonNode>> it = result.getMetadata().get().fields(); it.hasNext(); ) {
            Map.Entry<String, JsonNode> item = it.next();
            message.setHeader(String.format("X-Nile-%s", item.getKey()), item.getValue().asText());
        }

        ListenableFuture<SendResult<String, JsonNode>> future = kafkaTemplate.send(message.build());

        future.addCallback(stringMapSendResult -> {
            log.debug("Correctly sent message with key: {} to kafka", key.toString());
        }, throwable -> {
            log.error("Unable to send message with key {} error: {}", key, throwable.getMessage());
        });
        return key;
    }


    /**
     * Publish Schema on main log topic
     * @param schema
     */
    public void publishSchema(Schema schema) {
        MessageBuilder<JsonNode> message = MessageBuilder
                .withPayload(schema.getSchema())
                .setHeader(KafkaHeaders.TOPIC, mainlogTopic)
                .setHeader(KafkaHeaders.MESSAGE_KEY, schema.getUid());
        ListenableFuture<SendResult<String, JsonNode>> future = kafkaTemplate.send(message.build());

        future.addCallback(stringMapSendResult -> {
            log.debug("Correctly sent message with key: {} to kafka");

        }, throwable -> {
            log.error("Unable to send schema with key {} error: {}", schema.getUid(), throwable.getMessage());
        });
    }
}
