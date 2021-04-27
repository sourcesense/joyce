package com.sourcesense.nile.core.service;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sourcesense.nile.core.dto.Schema;
import com.sourcesense.nile.core.enumeration.IngestionAction;
import com.sourcesense.nile.core.enumeration.NotificationEvent;
import com.sourcesense.nile.core.model.NileURI;
import com.sourcesense.nile.core.utililty.constant.KafkaCustomHeaders;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.support.SendResult;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import java.util.Iterator;
import java.util.Map;

@Slf4j
@ConditionalOnProperty(value = "nile.mainlog-producer.enabled", havingValue = "true")
@Service
public class MainlogProducer extends KafkaMessageService<JsonNode> {

    @Value("${nile.kafka.mainlog-topic:mainlog}")
    String mainlogTopic;

    public MainlogProducer(ObjectMapper mapper, NotificationService notificationService, KafkaTemplate<String, JsonNode> kafkaTemplate) {
        super(mapper, notificationService, kafkaTemplate);
    }


    public NileURI removeContent(JsonNode metadata, NileURI uri){

        MessageBuilder<JsonNode> message = MessageBuilder
                .withPayload((JsonNode)mapper.createObjectNode())
                .setHeader(KafkaHeaders.TOPIC, mainlogTopic)
                .setHeader(KafkaCustomHeaders.MESSAGE_ACTION, IngestionAction.DELETE.toString())
                .setHeader(KafkaHeaders.MESSAGE_KEY, uri.toString());

        setMetadataHeaders(metadata, message);

        this.sendMessage(uri.toString(), message.build(), NotificationEvent.MAINLOG_PUBLISH_SUCCESS, NotificationEvent.MAINLOG_PUBLISH_FAILED);
        return uri;
    }

    /**
     * Publish to main log a processed content
     *
     * @param schema
     * @param metadata
     * @param uri
     * @param content
     * @return
     */
    public NileURI publishContent(Schema schema, JsonNode metadata, NileURI uri, JsonNode content) {


        // Set schema version
        ((ObjectNode)content).put("_schema_version_", schema.getVersion());
        ((ObjectNode)content).put("_schema_uid_", schema.getUid());
        ((ObjectNode)content).put("_schema_name_", schema.getName());
        ((ObjectNode)content).put("_schema_development_", schema.getDevelopment());

        MessageBuilder<JsonNode> message = MessageBuilder
                .withPayload(content)
                .setHeader(KafkaHeaders.TOPIC, mainlogTopic)
                .setHeader(KafkaCustomHeaders.MESSAGE_ACTION, IngestionAction.INSERT.toString())
                .setHeader(KafkaHeaders.MESSAGE_KEY, uri.toString());

        setMetadataHeaders(metadata, message);

        this.sendMessage(uri.toString(), message.build(), NotificationEvent.MAINLOG_PUBLISH_SUCCESS, NotificationEvent.MAINLOG_PUBLISH_FAILED);
        return uri;
    }

    private void setMetadataHeaders(JsonNode metadata, MessageBuilder<JsonNode> message) {
        for (Iterator<Map.Entry<String, JsonNode>> it = metadata.fields(); it.hasNext(); ) {
            Map.Entry<String, JsonNode> item = it.next();
            message.setHeader(String.format("X-Nile-%s", item.getKey()), item.getValue().asText());
        }
    }


    /**
     * Publish Schema on main log topic
     * @param schema
     */
    public void publishSchema(Schema schema) {
        MessageBuilder<JsonNode> message = MessageBuilder
                .withPayload(schema.getSchema())
                .setHeader(KafkaHeaders.TOPIC, mainlogTopic)
                .setHeader(KafkaHeaders.MESSAGE_KEY, schema.getUid())
                .setHeader(KafkaCustomHeaders.MESSAGE_ACTION, IngestionAction.INSERT.toString());

        this.sendMessage(schema.getUid(), message.build(), NotificationEvent.MAINLOG_PUBLISH_SUCCESS, NotificationEvent.MAINLOG_PUBLISH_FAILED);
    }

    //TODO: remove schema????

    @Override
    public void handleMessageSuccess(Message<JsonNode> message, SendResult<String, JsonNode> result) {
        log.debug("Correctly sent message: {} to mainlog", message);
    }

    @Override
    public void handleMessageFailure(Message<JsonNode> message, Throwable throwable) {
        log.error("Unable to send message=[{}] due to : [{}]", message, throwable.getMessage());
    }
}
