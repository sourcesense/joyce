package com.sourcesense.nile.sink.mongodb.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.client.result.DeleteResult;
import com.sourcesense.nile.core.annotation.ContentUri;
import com.sourcesense.nile.core.annotation.EventPayload;
import com.sourcesense.nile.core.annotation.Notify;
import com.sourcesense.nile.core.enumeration.KafkaCustomHeaders;
import com.sourcesense.nile.core.enumeration.NotificationEvent;
import com.sourcesense.nile.core.model.NileURI;
import com.sourcesense.nile.sink.mongodb.exception.MongodbSinkException;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class SinkService {
    private final MongoTemplate mongoTemplate;
    private final ObjectMapper mapper;

    @Notify(failureEvent = NotificationEvent.SINK_MONGODB_FAILED,
            successEvent = NotificationEvent.SINK_MONGODB_DELETED)
    public void deleteDocument(@ContentUri NileURI uri, String collection) throws MongodbSinkException {
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
    public void saveDocument(@EventPayload ObjectNode message, @ContentUri NileURI uri, String collection) throws MongodbSinkException {
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
    public NileURI getNileURI(@ContentUri String key) throws MongodbSinkException {
        return NileURI.createURI(key).orElseThrow(() -> new MongodbSinkException(String.format("uri [%s] is not a Valid Nile Uri", key)));
    }

    @Notify(failureEvent = NotificationEvent.SINK_MONGODB_FAILED)
    public String getCollection(@ContentUri String key, Map<String, String> headers) throws MongodbSinkException {
        if(headers.get(KafkaCustomHeaders.COLLECTION) == null){
            throw new MongodbSinkException(String.format("Missing %s header", KafkaCustomHeaders.COLLECTION));
        }
        return headers.get(KafkaCustomHeaders.COLLECTION);
    }
}
