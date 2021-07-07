package com.sourcesense.joyce.core.dao.utility;

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
	public Optional<SchemaEntity> mapFromData(Object data) {
		return Optional.of(objectMapper.convertValue(data, SchemaEntity.class));
	}
}
