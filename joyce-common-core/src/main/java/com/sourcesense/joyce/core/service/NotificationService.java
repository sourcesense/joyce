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
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sourcesense.joyce.core.configuration.NotificationServiceProperties;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.UUID;

@Builder
@Getter
class Notification {

	private final String source;
	private final String event;
	private final String rawUri;
	private final String contentUri;
	private final Boolean success;
	private final JsonNode metadata;
	private final JsonNode content;


	@Override
	public String toString() {
		return "Notification{" +
				"source='" + source + '\'' +
				", event='" + event + '\'' +
				", rawUri='" + rawUri + '\'' +
				", contentUri" + contentUri + '\'' +
				", success=" + success +
				", metadata=" + metadata +
				'}';
	}
}

@Slf4j
@ConditionalOnProperty(value = "joyce.notification-service.enabled", havingValue = "true")
@Service
public class NotificationService {

	private final ObjectMapper jsonMapper;
	private final NotificationServiceProperties properties;
	private final KafkaTemplate<String, JsonNode> kafkaTemplate;
	private final String topic;

	public NotificationService(
			ObjectMapper jsonMapper,
			NotificationServiceProperties properties,
			KafkaTemplate<String, JsonNode> kafkaTemplate,
			@Value("${joyce.kafka.notification.topic:joyce_notification}") String topic) {

		this.jsonMapper = jsonMapper;
		this.properties = properties;
		this.kafkaTemplate = kafkaTemplate;
		this.topic = topic;
	}


	public void ok(String rawUri, String contentUri, String event) {
		this.sendNotification(rawUri, contentUri, event, null, null, true);
	}

	public <T> void ok(String rawUri, String contentUri, String event, T metadata) {
		this.sendNotification(rawUri, contentUri, event, metadata, null, true);
	}

	public <T> void ok(String rawUri, String contentUri, String event, T metadata, T payload) {
		this.sendNotification(rawUri, contentUri, event, metadata, payload, true);
	}

	public void ko(String rawUri, String contentUri, String event, String error) {
		ObjectNode meta = jsonMapper.createObjectNode();
		meta.put("error", error);
		this.sendNotification(rawUri, contentUri, event, meta, null, false);
	}

	public <T> void ko(String rawUri, String contentUri, String event, T metadata) {
		this.sendNotification(rawUri, contentUri, event, metadata, null, false);
	}

	public <T> void ko(String rawUri, String contentUri, String event, T metadata, T payload) {
		this.sendNotification(rawUri, contentUri, event, metadata, payload, false);
	}

	private <T> void sendNotification(
			String rawUri,
			String contentUri,
			String event,
			T metadata,
			T payload,
			boolean success) {

		this.sendNotification(Notification.builder()
				.success(success)
				.rawUri(rawUri)
				.contentUri(contentUri)
				.event(event)
				.source(properties.getSource())
				.metadata(jsonMapper.valueToTree(metadata))
				.content(jsonMapper.valueToTree(payload))
				.build()
		);
	}

	private void sendNotification(Notification notification) {
		String uuid = UUID.randomUUID().toString().substring(0, 6);
		long timestamp = new Date().toInstant().toEpochMilli();
		String notificationKey = String.format("%d-%s", timestamp, uuid);
		Message<JsonNode> message = MessageBuilder
				.withPayload(jsonMapper.convertValue(notification, JsonNode.class))
				.setHeader(KafkaHeaders.TOPIC, topic)
				.setHeader(KafkaHeaders.MESSAGE_KEY, notificationKey)
				.build();

		kafkaTemplate.send(message).addCallback(
				stringMapSendResult -> log.debug("Sent notification message: {}", notification.toString()),
				throwable -> log.error(throwable.getMessage())
		);
	}
}
