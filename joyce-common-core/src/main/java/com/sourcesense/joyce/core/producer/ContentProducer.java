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
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sourcesense.joyce.core.enumeration.JoyceAction;
import com.sourcesense.joyce.core.enumeration.NotificationEvent;
import com.sourcesense.joyce.core.model.entity.JoyceKafkaKey;
import com.sourcesense.joyce.core.model.entity.JoyceKafkaKeyDefaultMetadata;
import com.sourcesense.joyce.core.model.entity.JoyceSchemaMetadata;
import com.sourcesense.joyce.core.model.entity.SchemaEntity;
import com.sourcesense.joyce.core.model.uri.JoyceContentURI;
import com.sourcesense.joyce.core.model.uri.JoyceDocumentURI;
import com.sourcesense.joyce.core.model.uri.JoyceSourceURI;
import com.sourcesense.joyce.core.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.support.SendResult;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Slf4j
@ConditionalOnProperty(value = "joyce.kafka.producer.enabled", havingValue = "true")
@Service
public class ContentProducer extends KafkaMessageProducer<String, JsonNode> {

	private final String contentTopic;
	private final NotificationService notificationService;

	public ContentProducer(
			ObjectMapper jsonMapper,
			NotificationService notificationService,
			KafkaTemplate<String, JsonNode> kafkaTemplate,
			@Value("${joyce.kafka.content.topic:joyce_content}") String contentTopic) {

		super(jsonMapper, kafkaTemplate);
		this.contentTopic = contentTopic;
		this.notificationService = notificationService;
	}

	public void publish(SchemaEntity schema, JoyceDocumentURI documentURI, JsonNode content, JoyceSchemaMetadata metadata) {
		this.sendInsertMessage(schema, documentURI, null, content, metadata);
	}

	public void publish(SchemaEntity schema, JoyceDocumentURI documentURI, JoyceSourceURI sourceURI, JsonNode content, JoyceSchemaMetadata metadata) {
		this.sendInsertMessage(schema, documentURI, sourceURI, content, metadata);
	}

	public void remove(JoyceDocumentURI documentURI, JoyceSchemaMetadata metadata) {
		this.sendRemovalMessage(documentURI, null, metadata);
	}

	public void remove(JoyceDocumentURI documentURI, JoyceSourceURI sourceURI, JoyceSchemaMetadata metadata) {
		this.sendRemovalMessage(documentURI, sourceURI, metadata);
	}

	public void remove(JoyceSourceURI sourceURI, JoyceSchemaMetadata metadata) {
		this.sendRemovalMessage(sourceURI, sourceURI, metadata);
	}

	/**
	 * Publish to main log a processed content
	 *
	 * @param schema     schema entity
	 * @param contentURI sourceURI
	 * @param sourceURI  documentURI
	 * @param content    message body
	 * @param metadata   message metadata
	 */
	private void sendInsertMessage(
			SchemaEntity schema,
			JoyceContentURI contentURI,
			JoyceSourceURI sourceURI,
			JsonNode content,
			JoyceSchemaMetadata metadata) {

		this.sendMessage(
				this.computeEnrichedContent(schema, sourceURI, content),
				this.buildKafkaKey(contentURI, sourceURI, metadata, JoyceAction.INSERT)
		);
	}

	private void sendRemovalMessage(JoyceContentURI contentURI, JoyceSourceURI sourceURI, JoyceSchemaMetadata metadata) {
		//Todo: The body of this message needs to be the full document body
		this.sendMessage(
				jsonMapper.createObjectNode(),
				this.buildKafkaKey(contentURI, sourceURI, metadata, JoyceAction.DELETE)
		);
	}

	private void sendMessage(JsonNode payload,	JoyceKafkaKey<JoyceContentURI, JoyceKafkaKeyDefaultMetadata> kafkaKey) {

		Message<JsonNode> message = MessageBuilder
				.withPayload(payload)
				.setHeader(KafkaHeaders.TOPIC, contentTopic)
				.setHeader(KafkaHeaders.MESSAGE_KEY, this.kafkaKeyToJson(kafkaKey))
				.build();

		this.sendMessage(kafkaKey.getUri(), kafkaKey.getMetadata().getSourceURI(), message);
	}

	private ObjectNode computeEnrichedContent(
			SchemaEntity schema,
			JoyceSourceURI sourceURI,
			JsonNode content) {

		ObjectNode content_metadata = jsonMapper.createObjectNode();
		content_metadata.put("schema_uri", schema.getUid().toString());
		content_metadata.put("schema_type", schema.getMetadata().getType());
		content_metadata.put("schema_development", schema.getMetadata().getDevelopment());
		if (Objects.nonNull(sourceURI)) {
			content_metadata.put("source_uri", sourceURI.toString());
		}

		ObjectNode enrichedContent = content.deepCopy();
		enrichedContent.set("_metadata_", content_metadata);
		return enrichedContent;
	}

	private JoyceKafkaKey<JoyceContentURI, JoyceKafkaKeyDefaultMetadata> buildKafkaKey(
			JoyceContentURI contentURI,
			JoyceSourceURI sourceURI,
			JoyceSchemaMetadata metadata,
			JoyceAction action) {

		return JoyceKafkaKey.<JoyceContentURI, JoyceKafkaKeyDefaultMetadata>builder()
				.uri(contentURI)
				.action(action)
				.metadata(JoyceKafkaKeyDefaultMetadata.builder()
						.sourceURI(sourceURI)
						.parentURI(metadata.getParent())
						.store(metadata.getStore())
						.build())
				.build();
	}

	@Override
	public void handleMessageSuccess(
			Message<JsonNode> message,
			SendResult<String, JsonNode> result,
			String contentURI,
			String sourceURI,
			JsonNode eventPayload,
			JsonNode eventMetadata) {

		log.debug("Correctly sent message: {} to content topic", message);
		notificationService.ok(
				contentURI,
				sourceURI,
				NotificationEvent.CONTENT_PUBLISH_SUCCESS,
				eventMetadata,
				eventPayload
		);
	}

	@Override
	public void handleMessageFailure(
			Message<JsonNode> message,
			Throwable throwable,
			String contentURI,
			String sourceURI,
			JsonNode eventPayload,
			JsonNode eventMetadata) {

		log.error("Unable to send message=[{}] due to : [{}]", message, throwable.getMessage());
		notificationService.ko(
				contentURI,
				sourceURI,
				NotificationEvent.CONTENT_PUBLISH_FAILED,
				eventMetadata,
				eventPayload
		);
	}
}
