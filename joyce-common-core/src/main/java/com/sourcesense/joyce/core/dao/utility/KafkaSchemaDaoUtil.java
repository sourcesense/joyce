package com.sourcesense.joyce.core.dao.utility;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sourcesense.joyce.core.exception.handler.CustomExceptionHandler;
import com.sourcesense.joyce.core.model.SchemaEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class KafkaSchemaDaoUtil implements SchemaDaoUtil{

	public static final String VALUE = "VALUE";

	private final ObjectMapper mapper;
	private final CustomExceptionHandler customExceptionHandler;

	@Override
	public Optional<SchemaEntity> mapFromData(Object data) {
		try {
			JsonNode json = (JsonNode) data;
			String value = json.get(VALUE).asText();
			SchemaEntity schema = mapper.readValue(value, SchemaEntity.class);
			return Optional.ofNullable(schema);

		} catch (JsonProcessingException exception) {
			customExceptionHandler.handleException(exception);
			return Optional.empty();
		}
	}
}
