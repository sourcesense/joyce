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
import com.sourcesense.joyce.core.enumeration.ImportAction;
import com.sourcesense.joyce.core.enumeration.KafkaCustomHeaders;
import com.sourcesense.joyce.core.enumeration.NotificationEvent;
import com.sourcesense.joyce.core.model.entity.JoyceSchemaMetadata;
import com.sourcesense.joyce.core.model.entity.SchemaEntity;
import com.sourcesense.joyce.core.model.uri.JoyceDocumentURI;
import com.sourcesense.joyce.core.model.uri.JoyceSourceURI;
import com.sourcesense.joyce.core.model.uri.JoyceURI;
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

import java.util.Optional;

@Slf4j
@ConditionalOnProperty(value = "joyce.kafka.producer.enabled", havingValue = "true")
@Service
public class ContentProducer extends KafkaMessageProducer<String,JsonNode> {

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

	public JoyceURI remove(JoyceURI sourceURI, JoyceSchemaMetadata metadata) {
		return this.sendRemovalMessage(sourceURI, sourceURI, metadata);
	}

	public JoyceURI remove(JoyceURI sourceURI, JoyceURI documentURI, JoyceSchemaMetadata metadata) {
		return this.sendRemovalMessage(sourceURI, documentURI, metadata);
	}

	public JoyceURI sendRemovalMessage(JoyceURI sourceURI, JoyceURI documentURI, JoyceSchemaMetadata metadata) {

		MessageBuilder<JsonNode> message = MessageBuilder
				.withPayload((JsonNode) jsonMapper.createObjectNode())
				.setHeader(KafkaHeaders.TOPIC, contentTopic)
				.setHeader(KafkaHeaders.MESSAGE_KEY, documentURI.toString())
				.setHeader(KafkaCustomHeaders.MESSAGE_ACTION, ImportAction.DELETE.toString())
				.setHeader(KafkaCustomHeaders.SOURCE_URI, sourceURI.toString());

		setMetadataHeaders(metadata, message);

		this.sendMessage(sourceURI.toString(), documentURI.toString(), message.build());
		return documentURI;
	}

	/**
	 * Publish to main log a processed content
	 *
	 * @param schema
	 * @param sourceURI
	 * @param documentURI
	 * @param content
	 * @param metadata
	 * @return
	 */
	public JoyceDocumentURI publish(
			SchemaEntity schema,
			JoyceSourceURI sourceURI,
			JoyceDocumentURI documentURI,
			JsonNode content,
			JoyceSchemaMetadata metadata) {

		JsonNode enrichedContent = this.computeEnrichedContent(schema, sourceURI, content);

		MessageBuilder<JsonNode> message = MessageBuilder
				.withPayload(enrichedContent)
				.setHeader(KafkaHeaders.TOPIC, contentTopic)
				.setHeader(KafkaCustomHeaders.MESSAGE_ACTION, ImportAction.INSERT.toString())
				.setHeader(KafkaHeaders.MESSAGE_KEY, documentURI.toString());

		setMetadataHeaders(metadata, message);

		this.sendMessage(
				sourceURI != null ? sourceURI.toString() : null,
				documentURI.toString(),
				message.build()
		);

		return documentURI;
	}

	private ObjectNode computeEnrichedContent(
			SchemaEntity schema,
			JoyceSourceURI sourceURI,
			JsonNode content) {

		// Set schema version
		//Todo: make Content metadata enum
		ObjectNode content_metadata = jsonMapper.createObjectNode();
		content_metadata.put("schema_uid", schema.getUid().toString());
		content_metadata.put("schema_name", schema.getMetadata().getName());
		content_metadata.put("schema_development", schema.getMetadata().getDevelopment());
		Optional.ofNullable(sourceURI).ifPresent(joyceURI -> content_metadata.put("source_uri", sourceURI.toString()));

		ObjectNode enrichedContent = content.deepCopy();
		enrichedContent.set("_metadata_", content_metadata);
		return enrichedContent;
	}

	private void setMetadataHeaders(JoyceSchemaMetadata metadata, MessageBuilder<JsonNode> message) {
		message.setHeader(KafkaCustomHeaders.COLLECTION, metadata.getCollection());
		message.setHeader(KafkaCustomHeaders.STORE_CONTENT, metadata.getStore().toString());
		message.setHeader(KafkaCustomHeaders.SCHEMA_NAME, metadata.getName());
		message.setHeader(KafkaCustomHeaders.SCHEMA_TYPE, metadata.getType());
		if (metadata.getParent() != null) {
			message.setHeader(KafkaCustomHeaders.SCHEMA_PARENT, metadata.getParent().toString());
		}
	}

	@Override
	public void handleMessageSuccess(
			Message<JsonNode> message,
			SendResult<String, JsonNode> result,
			String sourceURI,
			String documentURI,
			JsonNode eventPayload,
			JsonNode eventMetadata) {

		log.debug("Correctly sent message: {} to content topic", message);
		notificationService.ok(
				sourceURI,
				documentURI,
				NotificationEvent.CONTENT_PUBLISH_SUCCESS,
				eventMetadata,
				eventPayload
		);
	}

	@Override
	public void handleMessageFailure(
			Message<JsonNode> message,
			Throwable throwable,
			String sourceURI,
			String documentURI,
			JsonNode eventPayload,
			JsonNode eventMetadata) {

		log.error("Unable to send message=[{}] due to : [{}]", message, throwable.getMessage());
		notificationService.ko(
				sourceURI,
				documentURI,
				NotificationEvent.CONTENT_PUBLISH_FAILED,
				eventMetadata,
				eventPayload
		);
	}
}
