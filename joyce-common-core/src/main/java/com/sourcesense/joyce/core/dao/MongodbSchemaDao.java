package com.sourcesense.joyce.core.dao;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sourcesense.joyce.core.configuration.SchemaServiceProperties;
import com.sourcesense.joyce.core.dao.SchemaDao;
import com.sourcesense.joyce.core.dao.mongodb.SchemaDocument;
import com.sourcesense.joyce.core.dao.mongodb.SchemaRepository;
import com.sourcesense.joyce.core.enumeration.ImportAction;
import com.sourcesense.joyce.core.enumeration.KafkaCustomHeaders;
import com.sourcesense.joyce.core.mapper.SchemaMapper;
import com.sourcesense.joyce.core.model.SchemaEntity;
import com.sourcesense.joyce.core.utililty.KafkaUtility;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.config.TopicConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(value = "joyce.schema-service.database", havingValue = "mongodb")
@Component
public class MongodbSchemaDao implements SchemaDao {

	private final SchemaRepository schemaRepository;
	private final SchemaMapper schemaMapper;
	private final ObjectMapper objectMapper;
	private final KafkaTemplate<String,JsonNode> kafkaTemplate;
	private final SchemaServiceProperties schemaServiceProperties;
	private final KafkaAdmin kafkaAdmin;

	@PostConstruct
	void init() {
		try {
			KafkaUtility.addTopicIfNeeded(kafkaAdmin,
					schemaServiceProperties.getTopic(),
					schemaServiceProperties.getPartitions(),
					schemaServiceProperties.getReplicas(),
					schemaServiceProperties.getRetention(),
					TopicConfig.CLEANUP_POLICY_COMPACT);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
	}

	@Override
	public Optional<SchemaEntity> get(String id) {
		return schemaRepository.findById(id).map(schemaMapper::entityFromDocument);
	}

	@Override
	public List<SchemaEntity> getAll() {
		return schemaRepository.findAll().stream().map(schemaMapper::entityFromDocument).collect(Collectors.toList());
	}

	@Override
	public void save(SchemaEntity entity) {
		SchemaDocument doc = schemaMapper.documentFromEntity(entity);

		schemaRepository.save(doc);

		JsonNode content = objectMapper.convertValue(entity, JsonNode.class);
		MessageBuilder<JsonNode> message = MessageBuilder
				.withPayload(content)
				.setHeader(KafkaHeaders.TOPIC, schemaServiceProperties.getTopic())
				.setHeader(KafkaHeaders.MESSAGE_KEY, entity.getUid());

		kafkaTemplate.send(message.build());
		//TODO: notification of  new schema ???
	}

	@Override
	public void delete(SchemaEntity entity) {
		SchemaDocument doc = schemaMapper.documentFromEntity(entity);
		schemaRepository.delete(doc);

		JsonNode content = objectMapper.convertValue(entity, JsonNode.class);
		MessageBuilder<JsonNode> message = MessageBuilder
				.withPayload((JsonNode) objectMapper.createObjectNode())
				.setHeader(KafkaHeaders.TOPIC, schemaServiceProperties.getTopic())
				.setHeader(KafkaHeaders.MESSAGE_KEY, entity.getUid());

		kafkaTemplate.send(message.build());
		//TODO: notification of  schema deletion ???
	}
}
