package com.sourcesense.nile.sink.mongodb.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.client.result.DeleteResult;
import com.sourcesense.nile.core.enumeration.IngestionAction;
import com.sourcesense.nile.core.enumeration.KafkaCustomHeaders;
import com.sourcesense.nile.core.enumeration.NotificationEvent;
import com.sourcesense.nile.core.exceptions.InvalidNileUriException;
import com.sourcesense.nile.core.model.NileURI;
import com.sourcesense.nile.core.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
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
public class MainlogConsumer {

    private final MongoTemplate mongoTemplate;
    private final ObjectMapper mapper;
    private final NotificationService notificationService;

    @KafkaListener(topics = "${nile.kafka.mainlog-topic:mainlog}")
    public void receive(
            @Payload ObjectNode message,
            @Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) String key,
            @Headers Map<String, String> headers) {
        try {

            if(headers.get(KafkaCustomHeaders.COLLECTION) == null){
                throw new Exception(String.format("Missing %s header", KafkaCustomHeaders.COLLECTION));
            }
            String collection = headers.get(KafkaCustomHeaders.COLLECTION);
            IngestionAction action = IngestionAction.valueOf(headers.getOrDefault(KafkaCustomHeaders.MESSAGE_ACTION, IngestionAction.INSERT.name()));

            Optional<NileURI> uri = NileURI.createURI(key);
            if(uri.isEmpty()){
                throw new InvalidNileUriException(String.format("uri [%s] is not a Valid Nile Uri", key));
            }

            if (action.equals(IngestionAction.INSERT)){
                /**
                 * Insert document
                 */
                Document doc = new Document(mapper.convertValue(message, new TypeReference<>() {
                }));
                doc.put("_id", key);
                Document res = mongoTemplate.save(doc, collection);
                if (!res.isEmpty()){
                    notificationService.ok(key, NotificationEvent.SINK_MONGODB_STORED);
                } else {
                    notificationService.ko(key, NotificationEvent.SINK_MONGODB_FAILED, "Document is empty");
                }
            } else if (action.equals(IngestionAction.DELETE)){
                /**
                 * Delete document
                 */
                DeleteResult response;
                if (uri.get().getType().equals(NileURI.Type.RAW)){
                    response = mongoTemplate.remove(new Query(Criteria.where("_metadata_.raw_uri").is(key)), collection);
                } else { // if (uri.get().getType().equals(NileURI.Type.CONTENT)
                    response = mongoTemplate.remove(new Query(Criteria.where("_id").is(key)), collection);
                }

                if (response.getDeletedCount() > 0){
                    notificationService.ok(key, NotificationEvent.SINK_MONGODB_DELETED);
                } else {
                    notificationService.ko(key, NotificationEvent.SINK_MONGODB_FAILED, "No Document was deleted");
                }

            }

        } catch (Exception e){
            log.error("Cannot save message with key: {} cause: {}", key, e.getMessage());
            notificationService.ko(key, NotificationEvent.SINK_MONGODB_FAILED, e.getMessage());
        }

    }
}
