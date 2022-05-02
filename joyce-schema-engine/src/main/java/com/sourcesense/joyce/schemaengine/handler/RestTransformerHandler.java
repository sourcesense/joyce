package com.sourcesense.joyce.schemaengine.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sourcesense.joyce.schemaengine.annotation.SchemaTransformationHandler;
import com.sourcesense.joyce.schemaengine.exception.JoyceSchemaEngineException;
import com.sourcesense.joyce.schemaengine.model.handler.RestHandlerData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
@SchemaTransformationHandler(keyword = "$rest")
public class RestTransformerHandler extends JsonPathTransformerHandler {

	private final ObjectMapper jsonMapper;
	private final RestTemplate restTemplate;

	@Override
	public JsonNode process(String key, String type, JsonNode value, JsonNode source, Optional<JsonNode> metadata, Optional<Object> context) {

		RestHandlerData restHandlerData = jsonMapper.convertValue(value, RestHandlerData.class);

		ResponseEntity<JsonNode> response = restTemplate.exchange(
				restHandlerData.getUrl(),
				restHandlerData.getMethod(),
				new HttpEntity<>(restHandlerData.getBody(), restHandlerData.getHeaders()),
				JsonNode.class
		);

		if (HttpStatus.OK.equals(response.getStatusCode())) {
			return Optional.of(restHandlerData)
					.map(RestHandlerData::getExtract)
					.filter(extract -> StringUtils.isNotEmpty(extract.textValue()))
					.map(extract -> super.process(key, type, extract, response.getBody(), metadata, context))
					.orElse(response.getBody());

		} else {
			throw new JoyceSchemaEngineException(String.format(
					"An error happened while executing rest transformation handler. Response is '%s'.", response
			));
		}
	}
}

