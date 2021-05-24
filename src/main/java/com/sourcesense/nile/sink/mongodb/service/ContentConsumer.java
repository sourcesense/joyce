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
import org.apache.tomcat.util.net.WriteBuffer;
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
    final private SinkService sinkService;
    final private CustomExceptionHandler customExceptionHandler;

    @KafkaListener(topics = "${nile.kafka.content-topic:nile_content}")
    public void receive(
            @Payload ObjectNode message,
            @Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) String key,
            @Headers Map<String, String> headers) {
        try {

            String collection = sinkService.getCollection(key, headers);

            ImportAction action = ImportAction.valueOf(headers.getOrDefault(KafkaCustomHeaders.MESSAGE_ACTION, ImportAction.INSERT.name()));

            NileURI uri = sinkService.getNileURI(key);

            if (action.equals(ImportAction.INSERT)){
                sinkService.saveDocument(message, uri, collection);
            } else if (action.equals(ImportAction.DELETE)){
                sinkService.deleteDocument(uri, collection);
            }

        } catch (Exception exception){
            customExceptionHandler.handleException(exception);
        }

    }
}
