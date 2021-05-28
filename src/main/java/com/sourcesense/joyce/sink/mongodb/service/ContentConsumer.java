package com.sourcesense.joyce.sink.mongodb.service;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sourcesense.joyce.core.enumeration.ImportAction;
import com.sourcesense.joyce.core.enumeration.KafkaCustomHeaders;
import com.sourcesense.joyce.core.exception.handler.CustomExceptionHandler;
import com.sourcesense.joyce.core.model.JoyceURI;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
public class ContentConsumer {
    final private SinkService sinkService;
    final private CustomExceptionHandler customExceptionHandler;

    @KafkaListener(topics = "${joyce.kafka.content-topic:joyce_content}")
    public void receive(
            @Payload ObjectNode message,
            @Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) String key,
            @Headers Map<String, String> headers) {
        try {

            String collection = sinkService.getCollection(key, headers);

            ImportAction action = ImportAction.valueOf(headers.getOrDefault(KafkaCustomHeaders.MESSAGE_ACTION, ImportAction.INSERT.name()));

            JoyceURI uri = sinkService.getJoyceURI(key);

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
