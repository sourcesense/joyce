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
import com.sourcesense.nile.core.configuration.NotificationServiceProperties;
import com.sourcesense.nile.core.enumeration.NotificationEvent;
import io.confluent.ksql.api.client.Client;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@Builder
@Getter
class Notification {
    private String source;
    private NotificationEvent event;
    private String rawUri;
    private String contentUri;
    private Boolean success;
    private JsonNode metadata;
    private JsonNode content;


    @Override
    public String toString() {
        return "Notification{" +
                "source='" + source + '\'' +
                ", event='" + event + '\'' +
                ", rawUri='" + rawUri + '\'' +
                ", contentUri" + contentUri + '\'' +
                ", success=" + success +
                ", metadata=" + metadata +
                '}';
    }
}

@ConditionalOnProperty(value = "nile.notification-service.enabled", havingValue = "true")
@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationService {

    private final ObjectMapper mapper;
    private final NotificationServiceProperties properties;
    final private KafkaTemplate<String, JsonNode> kafkaTemplate;
    private final Client ksql;

    @PostConstruct
    void init() throws ExecutionException, InterruptedException {
        String createTable = String.format(
                "CREATE TABLE IF NOT EXISTS NILE_NOTIFICATION (\n" +
                        "     id VARCHAR PRIMARY KEY,\n" +
                        "     source VARCHAR\n," +
                        "     event VARCHAR\n," +
                        "     rawUri VARCHAR\n," +
                        "     contentUri VARCHAR\n," +
                        "     success BOOLEAN\n," +
                        "     metadata VARCHAR\n," +
                        "     content VARCHAR\n" +
                        "   ) WITH (\n" +
                        "     KAFKA_TOPIC = '%s', \n" +
                        "     PARTITIONS = %d, \n" +
                        "     VALUE_FORMAT = 'JSON'\n" +
                        "   );", properties.getTopic(), properties.getPartitions());
        ksql.executeStatement(createTable).get();
    }

    public void ok(String rawUri, String contentUri, NotificationEvent event) {
        this.sendNotification(rawUri, contentUri, event, null, null, true);
    }

    public <T> void ok(String rawUri, String contentUri, NotificationEvent event, T metadata) {
        this.sendNotification(rawUri, contentUri, event, metadata, null, true);
    }

    public <T> void ok(String rawUri, String contentUri, NotificationEvent event, T metadata, T payload) {
        this.sendNotification(rawUri, contentUri, event, metadata, payload, true);
    }

    public void ko(String rawUri, String contentUri, NotificationEvent event, String error) {
        ObjectNode meta = mapper.createObjectNode();
        meta.put("error", error);
        this.sendNotification(rawUri, contentUri, event, meta, null, false);
    }

    public <T> void ko(String rawUri, String contentUri, NotificationEvent event, T metadata) {
        this.sendNotification(rawUri, contentUri, event, metadata, null, false);
    }

    public <T> void ko(String rawUri, String contentUri, NotificationEvent event, T metadata, T payload) {
        this.sendNotification(rawUri, contentUri, event, metadata, payload, false);
    }

    private <T> void sendNotification(
            String rawUri,
            String contentUri,
            NotificationEvent event,
            T metadata,
            T payload,
            boolean success) {

        this.sendNotification(Notification.builder()
                .success(success)
                .rawUri(rawUri)
                .contentUri(contentUri)
                .event(event)
                .source(properties.getSource())
                .metadata(mapper.valueToTree(metadata))
                .content(mapper.valueToTree(payload))
                .build()
        );
    }

    private void sendNotification(Notification notification) {
        String uuid = UUID.randomUUID().toString().substring(0, 6);
        long timestamp = new Date().toInstant().toEpochMilli();
        String notificationKey = String.format("%d-%s", timestamp, uuid);
        Message<JsonNode> message = MessageBuilder
                .withPayload(mapper.convertValue(notification, JsonNode.class))
                .setHeader(KafkaHeaders.TOPIC, properties.getTopic())
                .setHeader(KafkaHeaders.MESSAGE_KEY, notificationKey)
                .build();

        kafkaTemplate.send(message).addCallback(
                stringMapSendResult -> log.debug("Sent notification message: {}", notification.toString()),
                throwable -> log.error(throwable.getMessage())
        );
    }
}
