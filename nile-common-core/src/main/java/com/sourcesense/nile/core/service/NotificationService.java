package com.sourcesense.nile.core.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sourcesense.nile.core.configuration.NotificationServiceProperties;
import com.sourcesense.nile.core.model.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@ConditionalOnProperty(value = "nile.notification-service.enabled", havingValue = "true")
@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationServiceProperties properties;
    private final ObjectMapper mapper;
    final private KafkaTemplate<String, Map> kafkaTemplate;

    public void sendNotification(Notification notification){
        try {
            log.info(mapper.writeValueAsString(notification));
        } catch (JsonProcessingException e) {
            log.error(e.getMessage());
        }
    }
}
