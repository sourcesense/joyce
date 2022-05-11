package com.sourcesense.joyce.core.producer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sourcesense.joyce.core.configuration.mongo.MongodbProperties;
import com.sourcesense.joyce.core.enumeration.JoyceAction;
import com.sourcesense.joyce.core.enumeration.NotificationEvent;
import com.sourcesense.joyce.core.model.entity.JoyceKafkaKey;
import com.sourcesense.joyce.core.model.entity.JoyceKafkaKeyDefaultMetadata;
import com.sourcesense.joyce.core.model.entity.SchemaEntity;
import com.sourcesense.joyce.core.model.uri.JoyceSchemaURI;
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
				jsonMapper.convertValue(schemaEntity, JsonNode.class),
				this.computeKafkaKey(schemaEntity, JoyceAction.INSERT)
		);
	}

	public void delete(SchemaEntity schemaEntity) {
		this.sendMessage(
				schemaEntity.getUid(),
				jsonMapper.createObjectNode(),
				this.computeKafkaKey(schemaEntity, JoyceAction.DELETE)
		);
	}

	private void sendMessage(
			JoyceSchemaURI schemaURI,
			JsonNode payload,
			JoyceKafkaKey<JoyceSchemaURI, JoyceKafkaKeyDefaultMetadata> kafkaKey) {

		Message<JsonNode> message = MessageBuilder
				.withPayload(payload)
				.setHeader(KafkaHeaders.TOPIC, mongodbProperties.getSchemaCollection())
				.setHeader(KafkaHeaders.MESSAGE_KEY, this.kafkaKeyToJson(kafkaKey))
				.build();

		this.sendMessage(schemaURI, message);
	}

	private JoyceKafkaKey<JoyceSchemaURI, JoyceKafkaKeyDefaultMetadata> computeKafkaKey(SchemaEntity schemaEntity, JoyceAction action) {
		return JoyceKafkaKey.<JoyceSchemaURI, JoyceKafkaKeyDefaultMetadata>builder()
				.uri(schemaEntity.getUid())
				.action(action)
				.metadata(JoyceKafkaKeyDefaultMetadata.builder()
						.parentURI(schemaEntity.getMetadata().getParent())
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

		log.debug("Correctly sent message: {} to schema topic", message);
		notificationService.ok(
				contentURI,
				sourceURI,
				NotificationEvent.SCHEMA_PUBLISH_SUCCESS,
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
				NotificationEvent.SCHEMA_PUBLISH_FAILED,
				eventMetadata,
				eventPayload
		);
	}
}
