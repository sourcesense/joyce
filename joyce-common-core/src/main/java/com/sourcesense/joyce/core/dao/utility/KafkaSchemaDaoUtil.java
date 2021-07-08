package com.sourcesense.joyce.core.dao.utility;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sourcesense.joyce.core.enumeration.KafkaCustomHeaders;
import com.sourcesense.joyce.core.exception.handler.CustomExceptionHandler;
import com.sourcesense.joyce.core.model.SchemaEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class KafkaSchemaDaoUtil implements SchemaDaoUtil {

	public static final String VALUE = "VALUE";

	private final ObjectMapper mapper;
	private final CustomExceptionHandler customExceptionHandler;

	public KafkaSchemaDaoUtil(
			@Qualifier("permissiveJsonMapper") ObjectMapper mapper,
			CustomExceptionHandler customExceptionHandler) {

		this.mapper = mapper;
		this.customExceptionHandler = customExceptionHandler;
	}

	@Override
	public Optional<SchemaEntity> mapFromData(JsonNode json) {
		return this.getValueFromData(json, SchemaEntity.class);
	}

	@Override
	public <T> Optional<T> mapFromData(JsonNode json, Class<T> clazz) {
		return this.getValueFromData(json, clazz);
	}

	private <T> Optional<T> getValueFromData(JsonNode json, Class<T> clazz) {
		try {
			String value = json.get(VALUE).asText();
			T schema = mapper.readValue(value, clazz);
			return Optional.ofNullable(schema);

		} catch (JsonProcessingException exception) {
			customExceptionHandler.handleException(exception);
			return Optional.empty();
		}
	}
}
