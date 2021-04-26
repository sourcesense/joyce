package com.sourcesense.nile.connectorcore.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sourcesense.nile.connectorcore.model.DataEntry;
import com.sourcesense.nile.core.service.KafkaMessageService;
import com.sourcesense.nile.core.service.NotificationService;
import com.sourcesense.nile.core.utililty.constant.KafkaCustomHeaders;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.support.SendResult;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;

@Log4j2
@Service
public class RawDataMessageService extends KafkaMessageService<JsonNode> {

    @Value("${nile.kafka.ingestion-topic}")
    private String ingestionTopic;

    public RawDataMessageService(
            ObjectMapper mapper,
            NotificationService notificationService,
            KafkaTemplate<String, JsonNode> kafkaTemplate) {

        super(mapper, notificationService, kafkaTemplate);
    }

    public ListenableFuture<SendResult<String, JsonNode>> sendMessageToOutputTopic(DataEntry dataEntry) {
        Message<JsonNode> rawDataMessage = this.getRawDataMessage(dataEntry);
        return this.sendMessage(dataEntry.getNileUri(), rawDataMessage);
    }

    private Message<JsonNode> getRawDataMessage(DataEntry entry) {
        return MessageBuilder
                .withPayload(entry.getData())
                .setHeader(KafkaHeaders.TOPIC, ingestionTopic)
                .setHeader(KafkaHeaders.MESSAGE_KEY, entry.getNileUri())
                .setHeader(KafkaCustomHeaders.MESSAGE_ACTION, entry.getAction())
                .setHeader(KafkaCustomHeaders.INGESTION_SCHEMA, entry.getSchemaKey())
                .build();
    }

    @Override
    public void handleMessageSuccess(Message<JsonNode> message, SendResult<String, JsonNode> result) {
        if (log.isDebugEnabled()) {
            log.debug("Sent message=[{}] with offset=[{}]", message, result.getRecordMetadata().offset());
        }
    }

    @Override
    public void handleMessageFailure(Message<JsonNode> message, Throwable throwable) {
        log.error("Unable to send message=[{}] due to : [{}]", message, throwable.getMessage());
    }
}
