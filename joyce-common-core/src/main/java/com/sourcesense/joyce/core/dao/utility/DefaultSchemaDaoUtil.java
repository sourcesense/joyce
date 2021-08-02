package com.sourcesense.joyce.core.dao.utility;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sourcesense.joyce.core.enumeration.SchemaServiceDatabase;
import com.sourcesense.joyce.core.model.SchemaEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Conditional(DefaultSchemaCondition.class)
@Component
@RequiredArgsConstructor
public class DefaultSchemaDaoUtil implements SchemaDaoUtil {

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

class DefaultSchemaCondition implements Condition {

	@Override
	public boolean matches(
			ConditionContext context,
			AnnotatedTypeMetadata annotatedTypeMetadata) {

		SchemaServiceDatabase schemaServiceDatabase = Optional.of(context)
				.map(ConditionContext::getEnvironment)
				.map(environment -> environment.getProperty("joyce.schema-service.database"))
				.map(String::toUpperCase)
				.map(SchemaServiceDatabase::getDatabaseFromValue)
				.orElse(SchemaServiceDatabase.UNDEFINED);

		switch (schemaServiceDatabase) {
			case KAFKA:
				return false;
			default:
				return true;
		}
	}
}
