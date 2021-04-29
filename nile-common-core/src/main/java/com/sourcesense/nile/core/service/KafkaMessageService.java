package com.sourcesense.nile.core.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sourcesense.nile.core.enumeration.NotificationEvent;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.messaging.Message;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

@RequiredArgsConstructor
public abstract class KafkaMessageService<T> {

    protected final ObjectMapper mapper;
    private final NotificationService notificationService;
    private final KafkaTemplate<String, T> kafkaTemplate;

    public abstract void handleMessageSuccess(Message<T> message, SendResult<String, T> result);

    public abstract void handleMessageFailure(Message<T> message, Throwable throwable);

    protected ListenableFuture<SendResult<String, T>> sendMessage(String messageKey, Message<T> message, NotificationEvent successEvent, NotificationEvent failEvent) {

        ListenableFuture<SendResult<String, T>> future = kafkaTemplate.send(message);
        future.addCallback(new ListenableFutureCallback<>() {

            @Override
            public void onSuccess(SendResult<String, T> result) {
                handleMessageSuccess(message, result);
                notificationService.ok(
                        messageKey,
                        successEvent,
                        formatRecordMetadata(result.getRecordMetadata()),
                        message
                );
            }

            @Override
            public void onFailure(Throwable throwable) {
                handleMessageFailure(message, throwable);
                notificationService.ko(
                        messageKey,
                        failEvent,
                        throwable.getMessage(),
                        message
                );
            }
        });

        return future;
    }

    private JsonNode formatRecordMetadata(RecordMetadata recordMetadata) {
        return mapper.createObjectNode()
                .put("topic", recordMetadata.topic())
                .put("partition", recordMetadata.partition())
                .put("offset", recordMetadata.offset())
                .put("timestamp", recordMetadata.timestamp());
    }
}
