package com.sourcesense.nile.mongodbprojector.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class MainlogConsumer {
    @Value("${nile.projection.headers.uid}")
    String UID_HEADER;

    @Value("${nile.projection.headers.collection}")
    String COLLECTION_HEADER;

    private final MongoTemplate mongoTemplate;

    @KafkaListener(topics = "${nile.kafka.mainlog-topic:mainlog}")
    public void receive(
            @Payload Map message,
            @Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) String key,
            @Headers Map<String, String> headers) {
        try {
            if(headers.get(UID_HEADER) == null){
                throw new Exception(String.format("Missing %s header", UID_HEADER));
            }

            if(headers.get(COLLECTION_HEADER) == null){
                throw new Exception(String.format("Missing %s header", COLLECTION_HEADER));
            }
            Document doc = new Document(message);
            doc.put("_id", message.get(headers.get(UID_HEADER)));
            Document res = mongoTemplate.save(doc, headers.get(COLLECTION_HEADER));

        } catch (Exception e){
            log.error("Cannot save message with key: {} cause: {}", key, e.getMessage());
            //TODO: integrate notificationEngine
        }

    }
}
