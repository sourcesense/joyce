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

@Builder
@Getter
class Notification {
    private String source;
    private NotificationEvent event;
    private String uid;
    private Boolean success;
    private JsonNode metadata;
    private JsonNode content;


    @Override
    public String toString() {
        return "Notification{" +
                "source='" + source + '\'' +
                ", event='" + event + '\'' +
                ", uid='" + uid + '\'' +
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

    public void ok(String uid, NotificationEvent event){
        this.sendNotification(uid, event, null, null, true);
    }

    public <T> void ok(String uid, NotificationEvent event, T metadata){
        this.sendNotification(uid, event, metadata, null, true);
    }

    public <T> void ok(String uid, NotificationEvent event, T metadata, T payload){
        this.sendNotification(uid, event, metadata, payload, true);
    }

    public void ko(String uid, NotificationEvent event, String error){
        ObjectNode meta = mapper.createObjectNode();
        meta.put("error", error);
        this.sendNotification(uid, event, meta, null, false);
    }

    public <T> void ko(String uid, NotificationEvent event, T metadata){
        this.sendNotification(uid, event, metadata, null, false);
    }

    public <T> void ko(String uid, NotificationEvent event, T metadata, T payload){
        this.sendNotification(uid, event, metadata, payload, false);
    }

    private  <T> void sendNotification(String uid, NotificationEvent event, T metadata, T payload, boolean success){
        Notification notification = Notification.builder()
                .success(success)
                .uid(uid)
                .event(event)
                .source(properties.getSource())
                .metadata(mapper.valueToTree(metadata))
                .content(mapper.valueToTree(payload))
                .build();
        this.sendNotification(notification);
    }

    private void sendNotification(Notification notification){
        Message<JsonNode> message = MessageBuilder
                .withPayload(mapper.convertValue(notification, JsonNode.class))
                .setHeader(KafkaHeaders.TOPIC, properties.getTopic())
                .setHeader(KafkaHeaders.MESSAGE_KEY, notification.getUid())
                .build();
        kafkaTemplate.send(message).addCallback(stringMapSendResult -> {
            log.debug("Sent notification message: {}", notification.toString());
        }, throwable -> {
            log.error(throwable.getMessage());
        });
    }
}
