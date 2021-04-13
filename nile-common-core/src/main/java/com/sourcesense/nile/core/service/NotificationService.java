package com.sourcesense.nile.core.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sourcesense.nile.core.configuration.NotificationServiceProperties;
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

import java.util.Map;

@Builder
@Getter
class Notification {
    private String source;
    private String event;
    private String uid;
    private Boolean success;
    private JsonNode metadata;


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

    private final NotificationServiceProperties properties;
    private final ObjectMapper mapper;
    final private KafkaTemplate<String, JsonNode> kafkaTemplate;

    public void ok(String uid, String event, JsonNode metadata){
        Notification notification = Notification.builder()
                .success(true)
                .source(properties.getSource())
                .event(event)
                .metadata(metadata)
                .uid(uid)
                .build();
        this.sendNotification(notification);
    }

    public void ko(String uid, String event, JsonNode metadata){
        Notification notification = Notification.builder()
                .success(false)
                .source(properties.getSource())
                .event(event)
                .metadata(metadata)
                .uid(uid)
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
            log.debug("Sent notification message", notification.toString());
        }, throwable -> {
            log.error(throwable.getMessage());
        });
    }
}
