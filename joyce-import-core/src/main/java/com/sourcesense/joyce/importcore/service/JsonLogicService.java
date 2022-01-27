package com.sourcesense.joyce.importcore.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.TextNode;
import com.sourcesense.joyce.core.model.JoyceSchemaMetadata;
import com.sourcesense.joyce.importcore.dto.JoyceSchemaImportMetadataExtra;
import com.sourcesense.joyce.importcore.exception.JsonLogicException;
import io.github.jamsesso.jsonlogic.JsonLogic;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
@Log4j2
@RequiredArgsConstructor
public class JsonLogicService {

	private final ObjectMapper mapper;
	private final JsonLogic jsonLogic;

	public Boolean filter(JsonNode document, JoyceSchemaMetadata metadata) {
		try {
			Optional<String> filter = this.computeFilterFromMetadata(metadata);
			Map<String, Object> data = mapper.convertValue(document, new TypeReference<>() {});

			return filter.isPresent()	? (Boolean) jsonLogic.apply(filter.get(), data)	: true;

		} catch (Exception exception) {
			throw new JsonLogicException(exception.getMessage());
		}
	}

	private Optional<String> computeFilterFromMetadata(JoyceSchemaMetadata metadata) {
		return Optional.ofNullable(metadata)
				.map(JoyceSchemaMetadata::getExtra)
				.map(extra -> mapper.convertValue(extra, JoyceSchemaImportMetadataExtra.class))
				.map(JoyceSchemaImportMetadataExtra::getFilter)
				.map(TextNode::asText);
	}
}
