package com.sourcesense.joyce.schemaengine.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sourcesense.joyce.schemaengine.annotation.SchemaTransformationHandler;
import com.sourcesense.joyce.schemaengine.exception.JoyceSchemaEngineException;
import com.sourcesense.joyce.schemaengine.model.dto.SchemaEngineContext;
import com.sourcesense.joyce.schemaengine.model.dto.handler.RestHandlerArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
@RequiredArgsConstructor
@SchemaTransformationHandler(keyword = "rest")
public class RestTransformerHandler implements SchemaTransformerHandler {

	private final ObjectMapper jsonMapper;
	private final RestTemplate restTemplate;


	@Override
	public JsonNode process(String key, String type, JsonNode args, SchemaEngineContext context) {
		RestHandlerArgs restHandlerArgs = jsonMapper.convertValue(args, RestHandlerArgs.class);
		try {
			ResponseEntity<JsonNode> response = restTemplate.exchange(
					restHandlerArgs.getUrl(),
					restHandlerArgs.getMethod(),
					new HttpEntity<>(restHandlerArgs.getBody(), restHandlerArgs.getHeaders()),
					JsonNode.class
			);

			if (!HttpStatus.OK.equals(response.getStatusCode())) {
				throw new JoyceSchemaEngineException(String.format("%d:%s",
						response.getStatusCode().value(),
						response.getBody()
				));
			}
			return response.getBody();

		} catch (Exception exception) {
			throw new JoyceSchemaEngineException(String.format(
					"An error happened while executing rest transformation handler, error message is [%s]",
					exception.getMessage()
			));
		}
	}
}

