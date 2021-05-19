/*
 *  Copyright 2021 Sourcesense Spa
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

package com.sourcesense.nile.core.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sourcesense.nile.core.annotation.*;
import com.sourcesense.nile.core.dto.Schema;
import com.sourcesense.nile.core.enumeration.ImportAction;
import com.sourcesense.nile.core.enumeration.KafkaCustomHeaders;
import com.sourcesense.nile.core.enumeration.NotificationEvent;
import com.sourcesense.nile.core.model.NileSchemaMetadata;
import com.sourcesense.nile.core.model.NileURI;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.support.SendResult;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

@Slf4j
@ConditionalOnProperty(value = "nile.mainlog-producer.enabled", havingValue = "true")
@Service
public class MainlogProducer extends KafkaMessageService<JsonNode> {

    @Value("${nile.kafka.mainlog-topic:mainlog}")
    String mainlogTopic;
    private final NotificationService notificationService;

    public MainlogProducer(
            ObjectMapper mapper,
            KafkaTemplate<String, JsonNode> kafkaTemplate,
            NotificationService notificationService) {

        super(mapper, kafkaTemplate);
        this.notificationService = notificationService;
    }

    public NileURI removeContent(NileURI rawUri, NileSchemaMetadata metadata) {
        return this.sendRemovalMessage(rawUri, rawUri, metadata);
    }

    public NileURI removeContent(NileURI rawUri, NileURI contentUri, NileSchemaMetadata metadata) {
        return this.sendRemovalMessage(rawUri, contentUri, metadata);
    }

    public NileURI sendRemovalMessage(NileURI rawUri, NileURI contentUri, NileSchemaMetadata metadata) {

        MessageBuilder<JsonNode> message = MessageBuilder
                .withPayload((JsonNode) mapper.createObjectNode())
                .setHeader(KafkaHeaders.TOPIC, mainlogTopic)
                .setHeader(KafkaHeaders.MESSAGE_KEY, contentUri.toString())
                .setHeader(KafkaCustomHeaders.MESSAGE_ACTION, ImportAction.DELETE.toString())
                .setHeader(KafkaCustomHeaders.RAW_URI, rawUri.toString());

        setMetadataHeaders(metadata, message);

        this.sendMessage(rawUri.toString(), contentUri.toString(), message.build());
        return contentUri;
    }

    /**
     * Publish to main log a processed content
     *
     * @param schema
     * @param rawUri
     * @param contentUri
     * @param content
     * @param metadata
     * @return
     */
    public NileURI publishContent(
            Schema schema,
            NileURI rawUri,
            NileURI contentUri,
            JsonNode content,
            NileSchemaMetadata metadata) {

        // Set schema version
        ObjectNode content_metadata = mapper.createObjectNode();
        content_metadata.put("schema_uid", schema.getUid());
        content_metadata.put("schema_name", schema.getName());
        content_metadata.put("schema_development", schema.getDevelopment());
        if (rawUri != null) {
            content_metadata.put("raw_uri", rawUri.toString());
        }
        ((ObjectNode) content).set("_metadata_", content_metadata);

        MessageBuilder<JsonNode> message = MessageBuilder
                .withPayload(content)
                .setHeader(KafkaHeaders.TOPIC, mainlogTopic)
                .setHeader(KafkaCustomHeaders.MESSAGE_ACTION, ImportAction.INSERT.toString())
                .setHeader(KafkaHeaders.MESSAGE_KEY, contentUri.toString());

        setMetadataHeaders(metadata, message);

        this.sendMessage(rawUri.toString(), contentUri.toString(), message.build());
        return contentUri;
    }

    private void setMetadataHeaders(NileSchemaMetadata metadata, MessageBuilder<JsonNode> message) {
        message.setHeader(KafkaCustomHeaders.COLLECTION, metadata.getCollection());
        message.setHeader(KafkaCustomHeaders.SCHEMA, metadata.getName());
        if (metadata.getParent() != null) {
            message.setHeader(KafkaCustomHeaders.PARENT, metadata.getParent().toString());
        }
        message.setHeader(KafkaCustomHeaders.SUBTYPE, metadata.getSubtype().toString());
    }


    /**
     * Publish Schema on main log topic
     *
     * @param schema
     */
    public void publishSchema(Schema schema) {
        MessageBuilder<JsonNode> message = MessageBuilder
                .withPayload(schema.getSchema())
                .setHeader(KafkaHeaders.TOPIC, mainlogTopic)
                .setHeader(KafkaHeaders.MESSAGE_KEY, schema.getUid())
                .setHeader(KafkaCustomHeaders.MESSAGE_ACTION, ImportAction.INSERT.toString());

        this.sendMessage(schema.getUid(), schema.getUid(), message.build());
    }

    //TODO: remove schema????

    @Override
    public void handleMessageSuccess(
            Message<JsonNode> message,
            SendResult<String, JsonNode> result,
            String rawUri,
            String contentUri,
            JsonNode eventPayload,
            JsonNode eventMetadata) {

        log.debug("Correctly sent message: {} to mainlog", message);
        notificationService.ok(
                rawUri,
                contentUri,
                NotificationEvent.MAINLOG_PUBLISH_SUCCESS,
                eventPayload,
                eventMetadata
        );
    }

    @Override
    public void handleMessageFailure(
            Message<JsonNode> message,
            Throwable throwable,
            String rawUri,
            String contentUri,
            JsonNode eventPayload,
            JsonNode eventMetadata) {

        log.error("Unable to send message=[{}] due to : [{}]", message, throwable.getMessage());
        notificationService.ko(
                rawUri,
                contentUri,
                NotificationEvent.MAINLOG_PUBLISH_SUCCESS,
                eventPayload,
                eventMetadata
        );
    }

}
