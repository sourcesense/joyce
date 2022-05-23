package com.sourcesense.joyce.importcore.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sourcesense.joyce.core.enumeration.ConnectorOperation;
import com.sourcesense.joyce.core.enumeration.uri.JoyceURIChannel;
import com.sourcesense.joyce.core.exception.RestException;
import com.sourcesense.joyce.core.model.dto.ConnectorOperationStatus;
import com.sourcesense.joyce.core.model.entity.JoyceSchemaMetadata;
import com.sourcesense.joyce.core.model.entity.JoyceSchemaMetadataExtraConnector;
import com.sourcesense.joyce.core.model.entity.SchemaEntity;
import com.sourcesense.joyce.core.model.uri.JoyceSourceURI;
import com.sourcesense.joyce.core.model.uri.JoyceURIFactory;
import com.sourcesense.joyce.importcore.dto.JoyceSchemaImportMetadataExtra;
import com.sourcesense.joyce.importcore.exception.ConnectorOperationException;
import com.sourcesense.joyce.schemacore.model.dto.SchemaSave;
import com.sourcesense.joyce.schemacore.service.SchemaService;
import com.sourcesense.joyce.schemaengine.templating.mustache.resolver.MustacheTemplateResolver;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class ConnectorService {

	private final static String NAME = "name";
	private final static String TOPIC = "topic";
	private final static String CONFIG = "config";
	private final static String JOYCE_KEY = "joyceKey";

	private final static String TRANSFORMS = "transforms";
	private final static String TRANSFORMS_JOYCE_KEY_SOURCE_UID_FIELD = "transforms.joyceKey.sourceUidField";
	private final static String TRANSFORMS_JOYCE_KEY_SOURCE_URI = "transforms.joyceKey.sourceUri";
	private final static String TRANSFORMS_JOYCE_KEY_TYPE = "transforms.joyceKey.type";

	private final String kafkaConnectHost;
	private final ObjectMapper jsonMapper;
	private final RestTemplate restTemplate;
	private final SchemaService schemaService;
	private final MustacheTemplateResolver mustacheTemplateResolver;

	public ConnectorService(
			ObjectMapper jsonMapper,
			RestTemplate restTemplate,
			SchemaService schemaService,
			MustacheTemplateResolver mustacheTemplateResolver,
			@Value("${joyce.connector-service.kafka-connect-host}") String kafkaConnectHost) {

		this.jsonMapper = jsonMapper;
		this.restTemplate = restTemplate;
		this.schemaService = schemaService;
		this.kafkaConnectHost = kafkaConnectHost;
		this.mustacheTemplateResolver = mustacheTemplateResolver;
	}

	public List<JoyceSchemaMetadataExtraConnector> getConnectors(
			String domain,
			String product,
			String name) {

		SchemaEntity schema = this.computeSchema(domain, product, name);
		return this.computeSchemaConnectors(schema);
	}

	public JsonNode getConnectorStatus(
			String domain,
			String product,
			String name,
			String connector) {

		String connectorStatusEndpoint = String.format("%s/status", this.computeConnectorsEndpoint(domain, product, name, connector));
		return this.executeRest(connectorStatusEndpoint, HttpMethod.GET, HttpStatus.OK);
	}

	public Boolean restartConnector(
			String domain,
			String product,
			String name,
			String connector) {

		String connectorRestartEndpoint = String.format("%s/restart", this.computeConnectorsEndpoint(domain, product, name, connector));
		this.executeRest(connectorRestartEndpoint, HttpMethod.POST, HttpStatus.NO_CONTENT);
		return true;
	}

	public Boolean pauseConnector(
			String domain,
			String product,
			String name,
			String connector) {

		String connectorPauseEndpoint = String.format("%s/pause", this.computeConnectorsEndpoint(domain, product, name, connector));
		this.executeRest(connectorPauseEndpoint, HttpMethod.PUT, HttpStatus.ACCEPTED);
		return true;
	}

	public Boolean resumeConnector(
			String domain,
			String product,
			String name,
			String connector) {

		String connectorResumeEndpoint = String.format("%s/resume", this.computeConnectorsEndpoint(domain, product, name, connector));
		this.executeRest(connectorResumeEndpoint, HttpMethod.PUT, HttpStatus.ACCEPTED);
		return true;
	}

	public Boolean restartConnectorTask(
			String domain,
			String product,
			String name,
			String connector,
			String task) {

		String connectorTaskRestartEndpoint = String.format(
				"%s/tasks/%s/restart",
				this.computeConnectorsEndpoint(domain, product, name, connector),
				task
		);
		this.executeRest(connectorTaskRestartEndpoint, HttpMethod.POST, HttpStatus.NO_CONTENT);
		return true;
	}

	public List<ConnectorOperationStatus> computeConnectors(SchemaSave schema) {
		Map<String, JoyceSchemaMetadataExtraConnector> newConnectors = this.computeNewConnectors(schema.getMetadata());
		List<String> existingConnectors = this.computeExistingConnectors(schema.getMetadata());
		List<String> totalConnectors = this.computeTotalConnectors(existingConnectors, newConnectors.keySet());
		return this.executeConnectorOperation(schema.getMetadata(), totalConnectors, existingConnectors, newConnectors);
	}

	public List<ConnectorOperationStatus> deleteConnectors(
			String domain,
			String product,
			String name) {

		SchemaEntity schema = this.computeSchema(domain, product, name);
		return this.deleteSchemaConnectors(schema);
	}

	private List<String> computeTotalConnectors(Collection<String> existingConnectors, Collection<String> newConnectors) {
		return Stream.of(existingConnectors, newConnectors)
				.flatMap(Collection::stream).distinct()
				.collect(Collectors.toList());
	}

	private Map<String, JoyceSchemaMetadataExtraConnector> computeNewConnectors(JoyceSchemaMetadata metadata) {
		return Optional.of(metadata)
				.map(JoyceSchemaMetadata::getExtra)
				.map(extra -> jsonMapper.convertValue(extra, JoyceSchemaImportMetadataExtra.class))
				.map(JoyceSchemaImportMetadataExtra::getConnectors)
				.orElse(new ArrayList<>()).stream()
				.collect(Collectors.toMap(
						connector -> this.computeConnectorName(metadata.getDomain(), metadata.getProduct(), metadata.getName(), connector.getName()),
						Function.identity()
				));
	}

	private List<String> computeExistingConnectors(JoyceSchemaMetadata metadata) {
		String connectorPrefix = this.computeConnectorPrefix(metadata.getDomain(), metadata.getProduct(), metadata.getName());
		List<String> existingConnectors = this.executeRestWithResponse(this.computeConnectorsEndpoint(), HttpMethod.GET, HttpStatus.OK);
		return existingConnectors.stream()
				.filter(connector -> connector.startsWith(connectorPrefix))
				.collect(Collectors.toList());
	}

	private SchemaEntity computeSchema(String domain, String product, String name) {
		return schemaService.getOrElseThrow(
				JoyceURIFactory.getInstance().createSchemaURIOrElseThrow(domain, product, name).toString()
		);
	}

	private List<JoyceSchemaMetadataExtraConnector> computeSchemaConnectors(SchemaEntity schema) {
		return Optional.ofNullable(schema)
				.map(SchemaEntity::getMetadata)
				.map(JoyceSchemaMetadata::getExtra)
				.map(extra -> jsonMapper.convertValue(extra, JoyceSchemaImportMetadataExtra.class))
				.map(JoyceSchemaImportMetadataExtra::getConnectors)
				.orElse(new ArrayList<>());
	}

	private List<ConnectorOperationStatus> executeConnectorOperation(
			JoyceSchemaMetadata metadata,
			List<String> totalConnectors,
			List<String> existingConnectors,
			Map<String, JoyceSchemaMetadataExtraConnector> newConnectors) {

		List<ConnectorOperationStatus> statusList = new ArrayList<>();
		for (String connectorName : totalConnectors) {
			try {
				boolean exists = existingConnectors.contains(connectorName);
				boolean inSchema = newConnectors.containsKey(connectorName);
				JoyceSchemaMetadataExtraConnector connector = newConnectors.get(connectorName);

				if (!exists && inSchema) {
					String endpoint = this.computeConnectorsEndpoint();
					JsonNode requestBody = this.computeCreateConnectorBody(connectorName, metadata, connector);
					this.executeConnectorOperation(endpoint, connectorName, requestBody, statusList, ConnectorOperation.CREATE);

				} else if (exists && inSchema) {
					String endpoint = String.format("%s/config", this.computeConnectorsEndpoint(connectorName));
					JsonNode requestBody = this.computeEnrichedConnectorConfig(connectorName, metadata, connector, ConnectorOperation.UPDATE);
					this.executeConnectorOperation(endpoint, connectorName, requestBody, statusList, ConnectorOperation.UPDATE);

				} else if (exists) {
					String endpoint = this.computeConnectorsEndpoint(connectorName);
					JsonNode emptyBody = jsonMapper.createObjectNode();
					this.executeConnectorOperation(endpoint, connectorName, emptyBody, statusList, ConnectorOperation.DELETE);
				}
			} catch (ConnectorOperationException exception) {
				statusList.add(ConnectorOperationStatus.builder()
						.name(exception.getConnector())
						.body(exception.getErrorMessage())
						.status(exception.getErrorStatus())
						.connectorOperation(exception.getConnectorOperation())
						.build()
				);
			}
		}
		return statusList;
	}

	private List<ConnectorOperationStatus> deleteSchemaConnectors(SchemaEntity schema) {
		JoyceSchemaMetadata metadata = schema.getMetadata();
		List<JoyceSchemaMetadataExtraConnector> connectors = this.computeSchemaConnectors(schema);
		List<ConnectorOperationStatus> statusList = new ArrayList<>();
		connectors.stream()
				.map(JoyceSchemaMetadataExtraConnector::getName)
				.map(connector -> this.computeConnectorName(metadata.getDomain(), metadata.getProduct(), metadata.getName(), connector))
				.forEach(connectorName -> this.executeConnectorOperation(
						this.computeConnectorsEndpoint(connectorName),
						connectorName,
						jsonMapper.createObjectNode(),
						statusList,
						ConnectorOperation.DELETE
				));
		return statusList;
	}

	private JsonNode computeCreateConnectorBody(
			String connectorName,
			JoyceSchemaMetadata metadata,
			JoyceSchemaMetadataExtraConnector connector) {

		ObjectNode createBody = jsonMapper.createObjectNode();
		JsonNode config = this.computeEnrichedConnectorConfig(connectorName, metadata, connector, ConnectorOperation.CREATE);
		createBody.put(NAME, connectorName);
		createBody.putPOJO(CONFIG, config);
		return createBody;
	}

	private JsonNode computeEnrichedConnectorConfig(
			String connectorName,
			JoyceSchemaMetadata metadata,
			JoyceSchemaMetadataExtraConnector connector,
			ConnectorOperation connectorOperation) {

		ObjectNode enrichedConfig = (ObjectNode) mustacheTemplateResolver.resolve(connector.getConfig());
		enrichedConfig.put(TOPIC, "joyce_import");

		JsonNode transforms = enrichedConfig.path(TRANSFORMS);
		boolean isJoyceKeyPresent = this.isJoyceKeyPresent(transforms);

		if (transforms.isTextual() && isJoyceKeyPresent) {
			return enrichedConfig;

		} else if (connector.getImportKeyUid() != null && transforms.isTextual() && !isJoyceKeyPresent) {

			String transform = String.format("%s,%s", transforms.asText(), JOYCE_KEY);
			this.addJoyceKeyTransformProperties(enrichedConfig, transform, metadata, connector);

		} else if (connector.getImportKeyUid() != null) {
			this.addJoyceKeyTransformProperties(enrichedConfig, JOYCE_KEY, metadata, connector);

		} else {
			throw new ConnectorOperationException("Field import uid key wasn't found for connector", connectorName, connectorOperation);
		}
		return enrichedConfig;
	}

	private boolean isJoyceKeyPresent(JsonNode transforms) {
		String[] transformations = transforms.asText().split(",");
		return Arrays.stream(transformations)
				.map(String::trim)
				.anyMatch(JOYCE_KEY::equals);
	}

	private void addJoyceKeyTransformProperties(
			ObjectNode enrichedConfig,
			String transforms,
			JoyceSchemaMetadata metadata,
			JoyceSchemaMetadataExtraConnector connector) {

		JoyceSourceURI sourceURI = JoyceURIFactory.getInstance().createSourceURIOrElseThrow(
				metadata.getDomain(),	metadata.getProduct(), metadata.getName(),
				JoyceURIChannel.CONNECT, connector.getName(), "[uid]"
		);
		enrichedConfig.put(TRANSFORMS, transforms);
		enrichedConfig.put(TRANSFORMS_JOYCE_KEY_SOURCE_UID_FIELD, connector.getImportKeyUid());
		enrichedConfig.put(TRANSFORMS_JOYCE_KEY_SOURCE_URI, sourceURI.toString());
		enrichedConfig.put(TRANSFORMS_JOYCE_KEY_TYPE, "com.sourcesense.joyce.connect.custom.InsertJoyceMessageKey");
	}

	private void executeConnectorOperation(
			String endpoint,
			String connectorName,
			JsonNode requestBody,
			List<ConnectorOperationStatus> statusList,
			ConnectorOperation connectorOperation) {

		ResponseEntity<JsonNode> response = this.executeRest(endpoint, requestBody, connectorOperation.getMethod());
		statusList.add(ConnectorOperationStatus.builder()
				.name(connectorName)
				.connectorOperation(connectorOperation)
				.status(response.getStatusCode())
				.body(response.getBody())
				.build()
		);
	}

	private <RES> RES executeRestWithResponse(
			String endpoint,
			HttpMethod method,
			HttpStatus requiredStatus) {

		return jsonMapper.convertValue(
				this.executeRest(endpoint, method, requiredStatus),
				new TypeReference<>() {}
		);
	}

	private JsonNode executeRest(
			String endpoint,
			HttpMethod method,
			HttpStatus requiredStatus) {

		JsonNode emptyBody = jsonMapper.createObjectNode();
		ResponseEntity<JsonNode> response = this.executeRest(endpoint, emptyBody, method);
		if (!requiredStatus.equals(response.getStatusCode())) {
			throw new RestException(endpoint, response);
		}
		return response.getBody();
	}

	private ResponseEntity<JsonNode> executeRest(
			String endpoint,
			JsonNode requestBody,
			HttpMethod method) {

		try {
			return restTemplate.exchange(endpoint, method, new HttpEntity<>(requestBody), JsonNode.class);

		} catch (RestClientResponseException exception) {
			return ResponseEntity
					.status(HttpStatus.valueOf(exception.getRawStatusCode()))
					.body(this.computeResponseBody(exception.getResponseBodyAsByteArray()));
		}
	}

	private JsonNode computeResponseBody(byte[] responseBody) {
		try {
			return jsonMapper.readTree(responseBody);

		} catch (Exception exception) {
			return JsonNodeFactory.instance.textNode("Impossible to read response body.");
		}
	}

	private String computeConnectorsEndpoint(String... parts) {
		switch (parts.length) {
			case 0:
				return String.format("http://%s/connectors", kafkaConnectHost);
			case 1:
				return String.format("http://%s/connectors/%s", kafkaConnectHost, parts[0]);
			case 4:
				return String.format("http://%s/connectors/%s", kafkaConnectHost, this.computeConnectorName(parts[0], parts[1], parts[2], parts[3]));
			default:
				return StringUtils.EMPTY;
		}
	}

	private String computeConnectorName(String domain, String product, String name, String connector) {
		return this.computeConnectorPrefix(domain, product, name) + connector;
	}

	private String computeConnectorPrefix(String domain, String product, String name) {
		return String.format("%s:%s:%s:", domain, product, name);
	}
}
