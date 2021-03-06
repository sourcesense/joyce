package com.sourcesense.joyce.schemaengine.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
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
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
@SchemaTransformationHandler(keyword = "$rest")
public class RestTransformerHandler extends JsonPathTransformerHandler {

	private final ObjectMapper mapper;
	private final RestTemplate restTemplate;
	private final MustacheFactory mf;

	@Override
	public JsonNode process(String key, String type, JsonNode value, JsonNode source, Optional<JsonNode> metadata, Optional<Object> context) {

		RestHandlerData restHandlerData = mapper.convertValue(value, RestHandlerData.class);
		// Resolve json paths inside vars

		Map<String, String> vars = this.computeVars(type, source, restHandlerData);
		ResponseEntity<JsonNode> response = this.computeResponse(restHandlerData, vars);

		if (HttpStatus.OK.equals(response.getStatusCode())) {
			return Optional.of(restHandlerData)
					.map(RestHandlerData::getExtract)
					.filter(extract -> StringUtils.isNotEmpty(extract.asText()))
					.map(extract -> super.process(key, type, extract, response.getBody(), metadata, context))
					.orElse(response.getBody());

		} else {
			throw new JoyceSchemaEngineException(
					String.format(
							"An error happened while executing rest transformation handler. Response is '%s'.", response.toString()
					)
			);
		}
	}

	private String applyTemplate(String template, Map<String, String> context) {
		if (template == null) {
			return null;
		}
		StringWriter writer = new StringWriter();
		Mustache mustache = mf.compile(new StringReader(template), template);
		mustache.execute(writer, context);
		return writer.toString();
	}

	private ResponseEntity<JsonNode> computeResponse(RestHandlerData restHandlerData, Map<String, String> vars) {
		// Apply templates
		String url = applyTemplate(restHandlerData.getUrl(), vars);
		String body = applyTemplate(restHandlerData.getBody(), vars);

		MultiValueMap<String, String> headers = new LinkedMultiValueMap<>(
				this.computeHeaders(restHandlerData, vars)
		);


		return restTemplate.exchange(
				url,
				restHandlerData.getMethod(),
				new HttpEntity<>(body, headers),
				JsonNode.class
		);
	}

	private Map<String, String> computeVars(
			String type,
			JsonNode source,
			RestHandlerData restHandlerData) {

		return Optional.ofNullable(restHandlerData.getVars())
				.stream().map(Map::entrySet)
				.flatMap(Set::stream)
				.collect(Collectors.toMap(
						Map.Entry::getKey,
						entry -> this.computeValueOrDefault(type, source, entry.getValue()).asText())
				);
	}

	private Map<String, List<String>> computeHeaders(
			RestHandlerData restHandlerData,
			Map<String, String> vars) {

		return restHandlerData.getHeaders()
				.entrySet().stream()
				.collect(Collectors.toMap(
						Map.Entry::getKey,
						o -> this.computeHeader(o.getValue(), vars)
				));
	}

	private List<String> computeHeader(List<String> header, Map<String, String> vars) {
		return header.stream()
				.map(headerValue -> this.applyTemplate(headerValue, vars))
				.collect(Collectors.toList());
	}
}

