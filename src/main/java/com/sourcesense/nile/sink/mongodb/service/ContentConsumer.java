package com.sourcesense.nile.sink.mongodb.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.client.result.DeleteResult;
import com.sourcesense.nile.core.annotation.ContentUri;
import com.sourcesense.nile.core.annotation.EventPayload;
import com.sourcesense.nile.core.annotation.Notify;
import com.sourcesense.nile.core.enumeration.ImportAction;
import com.sourcesense.nile.core.enumeration.KafkaCustomHeaders;
import com.sourcesense.nile.core.enumeration.NotificationEvent;
import com.sourcesense.nile.core.exception.InvalidNileUriException;
import com.sourcesense.nile.core.exception.handler.CustomExceptionHandler;
import com.sourcesense.nile.core.model.NileURI;
import com.sourcesense.nile.core.service.NotificationService;
import com.sourcesense.nile.sink.mongodb.exception.MongodbSinkException;
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
public class ContentConsumer {

    private final MongoTemplate mongoTemplate;
    private final ObjectMapper mapper;
    final private CustomExceptionHandler customExceptionHandler;

    @KafkaListener(topics = "${nile.kafka.content-topic:nile_content}")
    public void receive(
            @Payload ObjectNode message,
            @Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) String key,
            @Headers Map<String, String> headers) {
        try {

            String collection = getCollection(key, headers);

            ImportAction action = ImportAction.valueOf(headers.getOrDefault(KafkaCustomHeaders.MESSAGE_ACTION, ImportAction.INSERT.name()));

            NileURI uri = getNileURI(key);

            if (action.equals(ImportAction.INSERT)){
                saveDocument(message, uri, collection);
            } else if (action.equals(ImportAction.DELETE)){
                deleteDocument(uri, collection);
            }

        } catch (Exception exception){
            customExceptionHandler.handleException(exception);
        }

    }

    @Notify(failureEvent = NotificationEvent.SINK_MONGODB_FAILED,
            successEvent = NotificationEvent.SINK_MONGODB_DELETED)
    private void deleteDocument(@ContentUri NileURI uri, String collection) throws MongodbSinkException {
        /**
         * Delete document
         */
        DeleteResult response;
        if (uri.getType().equals(NileURI.Type.RAW)){
            response = mongoTemplate.remove(new Query(Criteria.where("_metadata_.raw_uri").is(uri.toString())), collection);
        } else { // if (uri.get().getType().equals(NileURI.Type.CONTENT)
            response = mongoTemplate.remove(new Query(Criteria.where("_id").is(uri.toString())), collection);
        }

        if (response.getDeletedCount() < 1){
            throw new MongodbSinkException(String.format("There is no docuemtn to delete with uri %s", uri.toString()));
        }
    }

    @Notify(failureEvent = NotificationEvent.SINK_MONGODB_FAILED,
            successEvent = NotificationEvent.SINK_MONGODB_STORED)
    private void saveDocument(@EventPayload ObjectNode message, @ContentUri NileURI uri, String collection) throws MongodbSinkException {
        /**
         * Insert document
         */
        Document doc = new Document(mapper.convertValue(message, new TypeReference<>() {
        }));
        doc.put("_id", uri.toString());
        Document res = mongoTemplate.save(doc, collection);
        if (res.isEmpty()){
            throw new MongodbSinkException("Document was not saved, result is empty");
        }
    }

    @Notify(failureEvent = NotificationEvent.SINK_MONGODB_FAILED)
    private NileURI getNileURI(@ContentUri String key) throws MongodbSinkException {
        return NileURI.createURI(key).orElseThrow(() -> new MongodbSinkException(String.format("uri [%s] is not a Valid Nile Uri", key)));
    }

    @Notify(failureEvent = NotificationEvent.SINK_MONGODB_FAILED)
    private String getCollection(@ContentUri String key, Map<String, String> headers) throws MongodbSinkException {
        if(headers.get(KafkaCustomHeaders.COLLECTION) == null){
            throw new MongodbSinkException(String.format("Missing %s header", KafkaCustomHeaders.COLLECTION));
        }
        return headers.get(KafkaCustomHeaders.COLLECTION);
    }
}
