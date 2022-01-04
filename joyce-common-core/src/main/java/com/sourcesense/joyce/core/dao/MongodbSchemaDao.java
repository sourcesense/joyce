package com.sourcesense.joyce.core.dao;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.DistinctIterable;
import com.sourcesense.joyce.core.configuration.SchemaServiceProperties;
import com.sourcesense.joyce.core.dao.mongodb.SchemaDocument;
import com.sourcesense.joyce.core.dao.mongodb.SchemaRepository;
import com.sourcesense.joyce.core.mapper.SchemaMapper;
import com.sourcesense.joyce.core.model.JoyceURI;
import com.sourcesense.joyce.core.model.SchemaEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;


@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(value = "joyce.schema-service.database", havingValue = "mongodb")
@Component
public class MongodbSchemaDao implements SchemaDao {

	private final SchemaRepository schemaRepository;
	private final MongoTemplate mongoTemplate;
	private final SchemaMapper schemaMapper;
	private final ObjectMapper objectMapper;
	private final KafkaTemplate<String, JsonNode> kafkaTemplate;
	private final SchemaServiceProperties schemaServiceProperties;

	@Override
	public Optional<SchemaEntity> get(String id) {
		return schemaRepository.findById(id).map(schemaMapper::entityFromDocument);
	}

	@Override
	public List<SchemaEntity> getAll(Boolean rootOnly) {
		List<SchemaDocument> schemas = rootOnly
				? schemaRepository.findAllWhereMetadata_ParentIsNull()
				: schemaRepository.findAll();

		return schemaMapper.entitiesFromDocuments(schemas);
	}

	@Override
	public void save(SchemaEntity entity) {
		SchemaDocument doc = schemaMapper.documentFromEntity(entity);

		schemaRepository.save(doc);

		JsonNode content = objectMapper.convertValue(entity, JsonNode.class);
		MessageBuilder<JsonNode> message = MessageBuilder
				.withPayload(content)
				.setHeader(KafkaHeaders.TOPIC, schemaServiceProperties.getCollection())
				.setHeader(KafkaHeaders.MESSAGE_KEY, entity.getUid());

		kafkaTemplate.send(message.build());
		//TODO: notification of  new schema ???
	}

	@Override
	public void delete(SchemaEntity entity) {
		SchemaDocument doc = schemaMapper.documentFromEntity(entity);
		schemaRepository.delete(doc);

		MessageBuilder<JsonNode> message = MessageBuilder
				.withPayload((JsonNode) objectMapper.createObjectNode())
				.setHeader(KafkaHeaders.TOPIC, schemaServiceProperties.getCollection())
				.setHeader(KafkaHeaders.MESSAGE_KEY, entity.getUid());

		kafkaTemplate.send(message.build());
		//TODO: notification of schema deletion ???
	}

	@Override
	public List<SchemaEntity> getAllBySubtypeAndNamespace(JoyceURI.Subtype subtype, String namespace, Boolean rootOnly) {
		List<SchemaDocument> schemas = rootOnly
				? schemaRepository.findAllByMetadata_SubtypeAndMetadata_NamespaceWhereMetadata_ParentIsNull(subtype.name(), namespace)
				: schemaRepository.findAllByMetadata_SubtypeAndMetadata_Namespace(subtype.name(), namespace);

		return schemaMapper.entitiesFromDocuments(schemas);
	}

	@Override
	public List<SchemaEntity> getAllByReportsNotEmpty() {
		List<SchemaDocument> schemas = schemaRepository.findAllByReportsNotEmpty();
		return schemaMapper.entitiesFromDocuments(schemas);
	}

	@Override
	public List<String> getAllNamespaces() {
		DistinctIterable<String> distinctIterable = mongoTemplate
				.getCollection(schemaServiceProperties.getCollection())
				.distinct("metadata.namespace", String.class);

		return StreamSupport.stream(distinctIterable.spliterator(), false)
				.collect(Collectors.toList());
	}
}
