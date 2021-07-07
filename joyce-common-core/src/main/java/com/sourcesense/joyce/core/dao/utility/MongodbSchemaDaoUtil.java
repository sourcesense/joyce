package com.sourcesense.joyce.core.dao.utility;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sourcesense.joyce.core.model.SchemaEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Optional;

@ConditionalOnProperty(value = "joyce.schema-service.database", havingValue = "mongodb")
@Component
@RequiredArgsConstructor
public class MongodbSchemaDaoUtil implements SchemaDaoUtil{

	private final ObjectMapper objectMapper;

	@Override
	public Optional<SchemaEntity> mapFromData(JsonNode json) {
		return Optional.of(objectMapper.convertValue(json, SchemaEntity.class));
	}

	@Override
	public <T> Optional<T> mapFromData(JsonNode json, Class<T> clazz) {
		return Optional.of(objectMapper.convertValue(json, clazz));
	}
}
