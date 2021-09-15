package com.sourcesense.joyce.importcore.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sourcesense.joyce.core.dto.ConnectorUpdateStatus;
import com.sourcesense.joyce.core.dto.SchemaSave;
import com.sourcesense.joyce.core.enumeration.ConnectorOperation;
import com.sourcesense.joyce.core.exception.RestException;
import com.sourcesense.joyce.core.model.JoyceSchemaMetadata;
import com.sourcesense.joyce.importcore.dto.JoyceSchemaImportMetadataExtraConnector;
import com.sourcesense.joyce.core.model.JoyceURI;
import com.sourcesense.joyce.importcore.dto.JoyceSchemaImportMetadataExtra;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class ConnectorService {

	private final static String NAME = "name";
	private final static String TOPIC = "topic";
	private final static String CONFIG = "config";
	private final static String TRANSFORMS = "transforms";
	private final static String TRANSFORMS_JOYCE_KEY_UID = "transforms.joyceKey.uid";
	private final static String TRANSFORMS_JOYCE_KEY_SOURCE = "transforms.joyceKey.source";
	private final static String TRANSFORMS_JOYCE_KEY_SCHEMA = "transforms.joyceKey.schema";
	private final static String TRANSFORMS_JOYCE_KEY_TYPE = "transforms.joyceKey.type";

	@Value("${joyce.connector-service.kafka-connect-url}")
	private String kafkaConnectUrl;

	private final ObjectMapper mapper;
	private final RestTemplate restTemplate;


	public JsonNode getConnectorStatus(
			String namespace,
			String name,
			String connector) {

		String connectorStatusEndpoint = String.format("%s/status", this.computeConnectorsEndpoint(namespace, name, connector));
		return this.executeRest(connectorStatusEndpoint, HttpMethod.GET, HttpStatus.OK);
	}

	public Boolean restartConnector(
			String namespace,
			String name,
			String connector) {

		String connectorRestartEndpoint = String.format("%s/restart", this.computeConnectorsEndpoint(namespace, name, connector));
		this.executeRest(connectorRestartEndpoint, HttpMethod.POST, HttpStatus.NO_CONTENT);
		return true;
	}

	public Boolean pauseConnector(
			String namespace,
			String name,
			String connector) {

		String connectorPauseEndpoint = String.format("%s/pause", this.computeConnectorsEndpoint(namespace, name, connector));
		this.executeRest(connectorPauseEndpoint, HttpMethod.PUT, HttpStatus.ACCEPTED);
		return true;
	}

	public Boolean resumeConnector(
			String namespace,
			String name,
			String connector) {

		String connectorResumeEndpoint = String.format("%s/resume", this.computeConnectorsEndpoint(namespace, name, connector));
		this.executeRest(connectorResumeEndpoint, HttpMethod.PUT, HttpStatus.ACCEPTED);
		return true;
	}

	public Boolean restartConnectorTask(
			String namespace,
			String name,
			String connector,
			String task) {

		String connectorTaskRestartEndpoint = String.format(
				"%s/tasks/%s/restart",
				this.computeConnectorsEndpoint(namespace, name, connector),
				task
		);
		this.executeRest(connectorTaskRestartEndpoint, HttpMethod.POST, HttpStatus.NO_CONTENT);
		return true;
	}

	public List<ConnectorUpdateStatus> saveOrUpdateConnectors(SchemaSave schema) {
		Map<String, JoyceSchemaImportMetadataExtraConnector> newConnectors = this.computeNewConnectors(schema.getMetadata());
		List<String> existingConnectors = this.executeRestWithResponse(this.computeConnectorsEndpoint(), HttpMethod.GET, HttpStatus.OK);
		List<String> totalConnectors = this.computeTotalConnectors(existingConnectors, newConnectors.keySet());
		return this.executeConnectorOperations(schema.getMetadata(), totalConnectors, existingConnectors, newConnectors);
	}

	private List<String> computeTotalConnectors(Collection<String> existingConnectors, Collection<String> newConnectors) {
		return Stream.of(existingConnectors, newConnectors)
				.flatMap(Collection::stream).distinct()
				.collect(Collectors.toList());
	}

	private Map<String, JoyceSchemaImportMetadataExtraConnector> computeNewConnectors(JoyceSchemaMetadata metadata) {
		return Optional.of(metadata)
				.map(JoyceSchemaMetadata::getExtra)
				.map(extra -> mapper.convertValue(extra, JoyceSchemaImportMetadataExtra.class))
				.map(JoyceSchemaImportMetadataExtra::getConnectors)
				.orElse(new ArrayList<>()).stream()
				.collect(Collectors.toMap(
						connector -> this.computeConnectorName(metadata.getNamespace(), metadata.getName(), connector.getName()),
						Function.identity()
				));
	}

	private List<ConnectorUpdateStatus> executeConnectorOperations(
			JoyceSchemaMetadata metadata,
			List<String> totalConnectors,
			List<String> existingConnectors,
			Map<String, JoyceSchemaImportMetadataExtraConnector> newConnectors) {

		List<ConnectorUpdateStatus> statusList = new ArrayList<>();
		for (String connectorName : totalConnectors) {
			boolean exists = existingConnectors.contains(connectorName);
			boolean inSchema = newConnectors.containsKey(connectorName);
			if (!exists && inSchema) {
				this.executeConnectorOperation(
						connectorName,
						this.computeConnectorsEndpoint(),
						this.computeCreateConnectorBody(connectorName, metadata, newConnectors.get(connectorName)),
						statusList,
						ConnectorOperation.CREATE
				);
			} else if (exists && inSchema) {
				this.executeConnectorOperation(
						connectorName,
						String.format("%s/config", this.computeConnectorsEndpoint(connectorName)),
						this.computeEnrichedConnectorConfig(metadata, newConnectors.get(connectorName)),
						statusList,
						ConnectorOperation.UPDATE
				);
			} else if (exists) {
				this.executeConnectorOperation(
						connectorName,
						this.computeConnectorsEndpoint(connectorName),
						mapper.createObjectNode(),
						statusList,
						ConnectorOperation.DELETE
				);
			}
		}
		return statusList;
	}

	private JsonNode computeCreateConnectorBody(
			String connectorName,
			JoyceSchemaMetadata metadata,
			JoyceSchemaImportMetadataExtraConnector connector) {

		ObjectNode createBody = mapper.createObjectNode();
		createBody.put(NAME, connectorName);
		createBody.putPOJO(CONFIG, this.computeEnrichedConnectorConfig(metadata, connector));
		return createBody;
	}

	private JsonNode computeEnrichedConnectorConfig(
			JoyceSchemaMetadata metadata,
			JoyceSchemaImportMetadataExtraConnector connector) {

		ObjectNode enrichedConfig = (ObjectNode) connector.getConfig();
		enrichedConfig.put(TOPIC, "joyce_import");
		JsonNode transforms = enrichedConfig.path(TRANSFORMS);
		if (!transforms.isTextual()) {
			this.addJoyceKeyTransformProperties(enrichedConfig, "joyceKey", metadata, connector);

		} else if(transforms.isTextual() && this.isJoyceKeyNotPresent(transforms)) {
			this.addJoyceKeyTransformProperties(enrichedConfig, String.format("%s,joyceKey", transforms.asText()), metadata, connector);
		}
		return enrichedConfig;
	}

	private void addJoyceKeyTransformProperties(
			ObjectNode enrichedConfig,
			String transforms,
			JoyceSchemaMetadata metadata,
			JoyceSchemaImportMetadataExtraConnector connector) {

		JoyceURI schemaUri = JoyceURI.makeNamespaced(JoyceURI.Type.SCHEMA, metadata.getSubtype(), metadata.getNamespace(), metadata.getName());
		enrichedConfig.put(TRANSFORMS, transforms);
		enrichedConfig.put(TRANSFORMS_JOYCE_KEY_UID, metadata.getUidKey());
		enrichedConfig.put(TRANSFORMS_JOYCE_KEY_SOURCE, connector.getName());
		enrichedConfig.put(TRANSFORMS_JOYCE_KEY_SCHEMA, schemaUri.toString());
		enrichedConfig.put(TRANSFORMS_JOYCE_KEY_TYPE, "com.sourcesense.joyce.connect.custom.InsertJoyceMessageKey");
	}

	private boolean isJoyceKeyNotPresent(JsonNode transforms) {
		String[] transformations = transforms.asText().split(",");
		return Arrays.stream(transformations)
				.map(String::trim)
				.noneMatch("joyceKey"::equals);
	}

	private <REQ> void executeConnectorOperation(
			String connector,
			String endpoint,
			REQ requestBody,
			List<ConnectorUpdateStatus> statusList,
			ConnectorOperation operation) {

		ResponseEntity<JsonNode> response = this.executeRest(endpoint, requestBody, operation.getMethod());
		statusList.add(ConnectorUpdateStatus.builder()
				.name(connector)
				.connectorOperation(operation)
				.status(response.getStatusCode())
				.body(response.getBody())
				.build()
		);
	}

	private <RES> RES executeRestWithResponse(
			String endpoint,
			HttpMethod method,
			HttpStatus requiredStatus) {

		return mapper.convertValue(
				this.executeRest(endpoint, method, requiredStatus),
				new TypeReference<>() {}
		);
	}

	private JsonNode executeRest(
			String endpoint,
			HttpMethod method,
			HttpStatus requiredStatus) {

		JsonNode emptyBody = mapper.createObjectNode();
		ResponseEntity<JsonNode> response = this.executeRest(endpoint, emptyBody, method);
		if (!requiredStatus.equals(response.getStatusCode())) {
			throw new RestException(endpoint, response);
		}
		return response.getBody();
	}

	private <REQ> ResponseEntity<JsonNode> executeRest(
			String endpoint,
			REQ requestBody,
			HttpMethod method) {

		try {
			return restTemplate.exchange(endpoint, method, new HttpEntity<>(requestBody), JsonNode.class);

		} catch (HttpClientErrorException exception) {
			return ResponseEntity
					.status(exception.getStatusCode())
					.body(this.computeResponseBody(exception.getResponseBodyAsByteArray()));
		}
	}

	private String computeConnectorsEndpoint(String... parts) {
		switch (parts.length) {
			case 0:
				return String.format("%s/connectors", kafkaConnectUrl);
			case 1:
				return String.format("%s/connectors/%s", kafkaConnectUrl, parts[0]);
			case 3:
				return String.format("%s/connectors/%s", kafkaConnectUrl, this.computeConnectorName(parts[0], parts[1], parts[2]));
			default:
				return StringUtils.EMPTY;
		}
	}

	private String computeConnectorName(String namespace, String name, String connector) {
		return namespace + JoyceURI.NAMESPACE_SEPARATOR + name + JoyceURI.NAMESPACE_SEPARATOR + connector;
	}

	private JsonNode computeResponseBody(byte[] responseBody) {
		try {
			return mapper.readTree(responseBody);

		} catch (Exception exception) {
			return mapper.createObjectNode()
					.put("error", "Impossible to read response body.");
		}
	}
}
