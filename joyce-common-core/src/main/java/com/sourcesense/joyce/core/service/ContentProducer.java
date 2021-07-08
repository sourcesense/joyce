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

package com.sourcesense.joyce.core.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sourcesense.joyce.core.dto.Schema;
import com.sourcesense.joyce.core.enumeration.ImportAction;
import com.sourcesense.joyce.core.enumeration.KafkaCustomHeaders;
import com.sourcesense.joyce.core.enumeration.NotificationEvent;
import com.sourcesense.joyce.core.model.JoyceSchemaMetadata;
import com.sourcesense.joyce.core.model.JoyceURI;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.support.SendResult;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@ConditionalOnProperty(value = "joyce.content-producer.enabled", havingValue = "true")
@Service
public class ContentProducer extends KafkaMessageService<JsonNode> {

    @Value("${joyce.kafka.content-topic:joyce_content}")
    String contentTopic;
    private final NotificationService notificationService;

    public ContentProducer(
            @Qualifier("jsonMapper") ObjectMapper mapper,
            KafkaTemplate<String, JsonNode> kafkaTemplate,
            NotificationService notificationService) {

        super(mapper, kafkaTemplate);
        this.notificationService = notificationService;
    }

    public JoyceURI removeContent(JoyceURI rawUri, JoyceSchemaMetadata metadata) {
        return this.sendRemovalMessage(rawUri, rawUri, metadata);
    }

    public JoyceURI removeContent(JoyceURI rawUri, JoyceURI contentUri, JoyceSchemaMetadata metadata) {
        return this.sendRemovalMessage(rawUri, contentUri, metadata);
    }

    public JoyceURI sendRemovalMessage(JoyceURI rawUri, JoyceURI contentUri, JoyceSchemaMetadata metadata) {

        MessageBuilder<JsonNode> message = MessageBuilder
                .withPayload((JsonNode) mapper.createObjectNode())
                .setHeader(KafkaHeaders.TOPIC, contentTopic)
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
    public JoyceURI publishContent(
            Schema schema,
            JoyceURI rawUri,
            JoyceURI contentUri,
            JsonNode content,
            JoyceSchemaMetadata metadata) {

        // Set schema version
        ObjectNode content_metadata = mapper.createObjectNode();
        content_metadata.put("schema_uid", schema.getUid());
        content_metadata.put("schema_name", schema.getName());
        content_metadata.put("schema_development", schema.getDevelopment());
			Optional.ofNullable(rawUri).ifPresent(joyceURI -> content_metadata.put("raw_uri", rawUri.toString()));


        ((ObjectNode) content).set("_metadata_", content_metadata);

        MessageBuilder<JsonNode> message = MessageBuilder
                .withPayload(content)
                .setHeader(KafkaHeaders.TOPIC, contentTopic)
                .setHeader(KafkaCustomHeaders.MESSAGE_ACTION, ImportAction.INSERT.toString())
                .setHeader(KafkaHeaders.MESSAGE_KEY, contentUri.toString());

        setMetadataHeaders(metadata, message);

        this.sendMessage((rawUri != null)?rawUri.toString():null, contentUri.toString(), message.build());
        return contentUri;
    }

    private void setMetadataHeaders(JoyceSchemaMetadata metadata, MessageBuilder<JsonNode> message) {
        message.setHeader(KafkaCustomHeaders.COLLECTION, metadata.getCollection());
        message.setHeader(KafkaCustomHeaders.SCHEMA, metadata.getName());
        message.setHeader(KafkaCustomHeaders.SUBTYPE, metadata.getSubtype().toString());
        message.setHeader(KafkaCustomHeaders.STORE_CONTENT, metadata.getStore().toString());
        if (metadata.getParent() != null) {
            message.setHeader(KafkaCustomHeaders.PARENT, metadata.getParent().toString());
        }
    }


    /**
     * Publish Schema on main log topic
     *
     * @param schema
     */
    public void publishSchema(Schema schema) {
        MessageBuilder<JsonNode> message = MessageBuilder
                .withPayload(schema.getSchema())
                .setHeader(KafkaHeaders.TOPIC, contentTopic)
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

        log.debug("Correctly sent message: {} to content topic", message);
        notificationService.ok(
                rawUri,
                contentUri,
                NotificationEvent.CONTENT_PUBLISH_SUCCESS,
                eventMetadata,
                eventPayload

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
                NotificationEvent.CONTENT_PUBLISH_FAILED,
                eventMetadata,
                eventPayload
        );
    }

}
