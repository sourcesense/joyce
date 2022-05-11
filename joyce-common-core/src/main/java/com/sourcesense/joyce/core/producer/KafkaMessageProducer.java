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

package com.sourcesense.joyce.core.producer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sourcesense.joyce.core.exception.handler.InvalidKafkaKeyException;
import com.sourcesense.joyce.core.model.entity.JoyceKafkaKey;
import com.sourcesense.joyce.core.model.entity.JoyceKafkaKeyMetadata;
import com.sourcesense.joyce.core.model.uri.*;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.logging.log4j.util.Strings;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.messaging.Message;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import java.util.Objects;

@RequiredArgsConstructor
public abstract class KafkaMessageProducer<Z, T> {

	protected final ObjectMapper jsonMapper;
	private final KafkaTemplate<Z, T> kafkaTemplate;

	public abstract void handleMessageSuccess(
			Message<T> message,
			SendResult<Z, T> result,
			String contentURI,
			String sourceURI,
			T eventPayload,
			JsonNode eventMetadata
	);

	public abstract void handleMessageFailure(
			Message<T> message,
			Throwable throwable,
			String contentURI,
			String sourceURI,
			T eventPayload,
			JsonNode eventMetadata);

	protected ListenableFuture<SendResult<Z, T>> sendMessage(JoyceURI id, Message<T> message) {
		return this.sendMessage(id, null, message);
	}

	protected ListenableFuture<SendResult<Z, T>> sendMessage(JoyceURI id, JoyceURI source, Message<T> message) {
		return this.sendMessage(
				Objects.nonNull(id) ? id.toString(): Strings.EMPTY,
				Objects.nonNull(source) ? source.toString() : Strings.EMPTY,
				message
		);
	}

	protected ListenableFuture<SendResult<Z, T>> sendMessage(String id, Message<T> message) {
		return this.sendMessage(id, Strings.EMPTY, message);
	}

	private ListenableFuture<SendResult<Z, T>> sendMessage(String id, String source, Message<T> message) {
		ListenableFuture<SendResult<Z, T>> future = kafkaTemplate.send(message);
		future.addCallback(new ListenableFutureCallback<>() {

			@Override
			public void onSuccess(SendResult<Z, T> result) {
				handleMessageSuccess(
						message, result, id, source,
						message.getPayload(), formatRecordMetadata(result.getRecordMetadata())
				);
			}

			@Override
			public void onFailure(@NonNull Throwable throwable) {
				handleMessageFailure(
						message, throwable, id, source,
						message.getPayload(), formatError(throwable.getMessage())
				);
			}
		});

		return future;
	}

	protected JsonNode formatRecordMetadata(RecordMetadata recordMetadata) {
		return jsonMapper.createObjectNode()
				.put("topic", recordMetadata.topic())
				.put("partition", recordMetadata.partition())
				.put("offset", recordMetadata.offset())
				.put("timestamp", recordMetadata.timestamp());
	}

	protected JsonNode formatError(String error) {
		return jsonMapper.createObjectNode()
				.put("error", error);
	}

	protected <J extends JoyceURI, M extends JoyceKafkaKeyMetadata> String kafkaKeyToJson(JoyceKafkaKey<J, M> kafkaKey) {
		try{
			return jsonMapper.writeValueAsString(kafkaKey);

		} catch (Exception exception) {
			throw new InvalidKafkaKeyException(String.format(
					"Impossible to convert to json kafkaKey '%s'",kafkaKey
			));
		}
	}
}
