/*
 *  Copyright 2021 Sourcesense Spa
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sourcesense.joyce.core.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    private final KafkaTemplate<String, T> kafkaTemplate;

    public abstract void handleMessageSuccess(
            Message<T> message,
            SendResult<String, T> result,
            String rawUri,
            String contentUri,
            T eventPayload,
            JsonNode eventMetadata
    );

    public abstract void handleMessageFailure(
            Message<T> message,
            Throwable throwable,
            String rawUri,
            String contentUri,
            T eventPayload,
            JsonNode eventMetadata);

    protected ListenableFuture<SendResult<String, T>> sendMessage(String rawUri, String contentUri, Message<T> message) {
			ListenableFuture<SendResult<String, T>> future = kafkaTemplate.send(message);
			future.addCallback(new ListenableFutureCallback<>() {

				@Override
				public void onSuccess(SendResult<String, T> result) {
					handleMessageSuccess(
							message,
							result,
							rawUri,
							contentUri,
							message.getPayload(),
							formatRecordMetadata(result.getRecordMetadata())
					);
				}

				@Override
				public void onFailure(Throwable throwable) {
					handleMessageFailure(
							message,
							throwable,
							rawUri,
							contentUri,
							message.getPayload(),
							formatError(throwable.getMessage())
					);
				}
			});

			return future;
    }

    protected JsonNode formatRecordMetadata(RecordMetadata recordMetadata) {
        return mapper.createObjectNode()
                .put("topic", recordMetadata.topic())
                .put("partition", recordMetadata.partition())
                .put("offset", recordMetadata.offset())
                .put("timestamp", recordMetadata.timestamp());
    }

    protected JsonNode formatError(String error) {
        return mapper.createObjectNode()
                .put("error", error);
    }
}
