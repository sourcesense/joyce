package com.sourcesense.joyce.core.producer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sourcesense.joyce.core.configuration.mongo.MongodbProperties;
import com.sourcesense.joyce.core.enumeration.ImportAction;
import com.sourcesense.joyce.core.enumeration.KafkaCustomHeaders;
import com.sourcesense.joyce.core.enumeration.NotificationEvent;
import com.sourcesense.joyce.core.model.entity.SchemaEntity;
import com.sourcesense.joyce.core.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.support.SendResult;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@ConditionalOnProperty(value = "joyce.kafka.producer.enabled", havingValue = "true")
public class SchemaProducer extends KafkaMessageProducer<String,JsonNode> {

	private final NotificationService notificationService;
	private final MongodbProperties mongodbProperties;


	public SchemaProducer(
			ObjectMapper jsonMapper,
			KafkaTemplate<String, JsonNode> kafkaTemplate,
			NotificationService notificationService,
			MongodbProperties mongodbProperties) {

		super(jsonMapper, kafkaTemplate);
		this.notificationService = notificationService;
		this.mongodbProperties = mongodbProperties;
	}

	public void publish(SchemaEntity schemaEntity) {
		this.sendMessage(
				schemaEntity.getUid(),
				schemaEntity.getUid(),
				this.buildSchemaMessage(schemaEntity, jsonMapper.convertValue(schemaEntity, JsonNode.class), ImportAction.INSERT.name())
		);
	}

	public void delete(SchemaEntity schemaEntity) {
		this.sendMessage(
				schemaEntity.getUid(),
				schemaEntity.getUid(),
				this.buildSchemaMessage(schemaEntity, jsonMapper.createObjectNode(), ImportAction.DELETE.name())
		);
	}

	private Message<JsonNode> buildSchemaMessage(SchemaEntity schemaEntity, JsonNode payload, Object action) {
		return MessageBuilder
				.withPayload(payload)
				.setHeader(KafkaHeaders.TOPIC, mongodbProperties.getSchemaCollection())
				.setHeader(KafkaCustomHeaders.COLLECTION, schemaEntity.getMetadata().getNamespacedCollection())
				.setHeader(KafkaCustomHeaders.MESSAGE_ACTION, action)
				.setHeader(KafkaHeaders.MESSAGE_KEY, schemaEntity.getUid())
				.build();
	}

	@Override
	public void handleMessageSuccess(
			Message<JsonNode> message,
			SendResult<String, JsonNode> result,
			String rawUri,
			String contentUri,
			JsonNode eventPayload,
			JsonNode eventMetadata) {

		log.debug("Correctly sent message: {} to schema topic", message);
		notificationService.ok(
				rawUri,
				contentUri,
				NotificationEvent.SCHEMA_PUBLISH_SUCCESS,
				eventMetadata,
				eventPayload
		);
	}

	@Override
	public void handleMessageFailure(
			Message<JsonNode> message,
			Throwable throwable,
			String rawUri,
			String contentUri,
			JsonNode eventPayload,
			JsonNode eventMetadata) {

		log.error("Unable to send message=[{}] due to : [{}]", message, throwable.getMessage());
		notificationService.ko(
				rawUri,
				contentUri,
				NotificationEvent.SCHEMA_PUBLISH_FAILED,
				eventMetadata,
				eventPayload
		);
	}
}
