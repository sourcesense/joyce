package com.sourcesense.joyce.importcore.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sourcesense.joyce.core.dto.ConnectorOperationStatus;
import com.sourcesense.joyce.core.dto.SchemaSave;
import com.sourcesense.joyce.core.enumeration.ConnectorOperation;
import com.sourcesense.joyce.core.exception.RestException;
import com.sourcesense.joyce.core.model.JoyceSchemaMetadata;
import com.sourcesense.joyce.core.model.JoyceSchemaMetadataExtraConnector;
import com.sourcesense.joyce.core.model.JoyceURI;
import com.sourcesense.joyce.core.model.SchemaEntity;
import com.sourcesense.joyce.core.service.SchemaService;
import com.sourcesense.joyce.importcore.dto.JoyceSchemaImportMetadataExtra;
import com.sourcesense.joyce.importcore.exception.ConnectorOperationException;
import lombok.RequiredArgsConstructor;
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

import static com.sourcesense.joyce.core.model.JoyceURI.NAMESPACE_SEPARATOR;

@Service
@RequiredArgsConstructor
public class ConnectorService {

	private final static String NAME = "name";
	private final static String TOPIC = "topic";
	private final static String CONFIG = "config";
	private final static String JOYCE_KEY = "joyceKey";
	private final static String TRANSFORMS = "transforms";
	private final static String TRANSFORMS_JOYCE_KEY_UID = "transforms.joyceKey.uid";
	private final static String TRANSFORMS_JOYCE_KEY_SOURCE = "transforms.joyceKey.source";
	private final static String TRANSFORMS_JOYCE_KEY_SCHEMA = "transforms.joyceKey.schema";
	private final static String TRANSFORMS_JOYCE_KEY_TYPE = "transforms.joyceKey.type";

	@Value("${joyce.connector-service.kafka-connect-host}")
	private String kafkaConnectHost;

	private final ObjectMapper mapper;
	private final RestTemplate restTemplate;
	private final SchemaService schemaService;


	public List<JoyceSchemaMetadataExtraConnector> getConnectors(
			String subtype,
			String namespace,
			String name) {

		SchemaEntity schema = this.computeSchema(subtype, namespace, name);
		return this.computeSchemaConnectors(schema);
	}

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

	public List<ConnectorOperationStatus> computeConnectors(SchemaSave schema) {
		Map<String, JoyceSchemaMetadataExtraConnector> newConnectors = this.computeNewConnectors(schema.getMetadata());
		List<String> existingConnectors = this.computeExistingConnectors(schema.getMetadata());
		List<String> totalConnectors = this.computeTotalConnectors(existingConnectors, newConnectors.keySet());
		return this.executeConnectorOperation(schema.getMetadata(), totalConnectors, existingConnectors, newConnectors);
	}

	public List<ConnectorOperationStatus> deleteConnectors(
			String subtype,
			String namespace,
			String name) {

		SchemaEntity schema = this.computeSchema(subtype, namespace, name);
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
				.map(extra -> mapper.convertValue(extra, JoyceSchemaImportMetadataExtra.class))
				.map(JoyceSchemaImportMetadataExtra::getConnectors)
				.orElse(new ArrayList<>()).stream()
				.collect(Collectors.toMap(
						connector -> this.computeConnectorName(metadata.getNamespace(), metadata.getName(), connector.getName()),
						Function.identity()
				));
	}

	private List<String> computeExistingConnectors(JoyceSchemaMetadata metadata) {
		String connectorPrefix = this.computeConnectorPrefix(metadata.getNamespace(), metadata.getName());
		List<String> existingConnectors = this.executeRestWithResponse(this.computeConnectorsEndpoint(), HttpMethod.GET, HttpStatus.OK);
		return existingConnectors.stream()
				.filter(connector -> connector.startsWith(connectorPrefix))
				.collect(Collectors.toList());
	}

	private SchemaEntity computeSchema(String subtype, String namespace, String name) {
		JoyceURI.Subtype joyceUriSubtype = JoyceURI.Subtype.getOrElseThrow(subtype);
		JoyceURI schemaUri = JoyceURI.makeNamespaced(JoyceURI.Type.SCHEMA, joyceUriSubtype, namespace, name);
		return schemaService.getOrElseThrow(schemaUri.toString());
	}

	private List<JoyceSchemaMetadataExtraConnector> computeSchemaConnectors(SchemaEntity schema) {
		return Optional.ofNullable(schema)
				.map(SchemaEntity::getMetadata)
				.map(JoyceSchemaMetadata::getExtra)
				.map(extra -> mapper.convertValue(extra, JoyceSchemaImportMetadataExtra.class))
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
					JsonNode emptyBody = mapper.createObjectNode();
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
				.map(connector -> this.computeConnectorName(metadata.getNamespace(), metadata.getName(), connector))
				.forEach(connectorName -> this.executeConnectorOperation(
						this.computeConnectorsEndpoint(connectorName),
						connectorName,
						mapper.createObjectNode(),
						statusList,
						ConnectorOperation.DELETE
				));
		return statusList;
	}

	private JsonNode computeCreateConnectorBody(
			String connectorName,
			JoyceSchemaMetadata metadata,
			JoyceSchemaMetadataExtraConnector connector) {

		ObjectNode createBody = mapper.createObjectNode();
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

		ObjectNode enrichedConfig = connector.getConfig().deepCopy();
		enrichedConfig.put(TOPIC, "joyce_import");
		JsonNode transforms = enrichedConfig.path(TRANSFORMS);
		boolean isJoyceKeyPresent = this.isJoyceKeyPresent(transforms);
		if (transforms.isTextual() && isJoyceKeyPresent) {
			return enrichedConfig;

		} else if (connector.getImportKeyUid() != null
				&& transforms.isTextual()
				&& !isJoyceKeyPresent) {

			String transform = String.format("%s,%s", transforms.asText(), JOYCE_KEY);
			this.addJoyceKeyTransformProperties(enrichedConfig, transform, metadata, connector);

		} else if(connector.getImportKeyUid() != null) {
			this.addJoyceKeyTransformProperties(enrichedConfig, JOYCE_KEY, metadata, connector);

		} else {
			throw new ConnectorOperationException(
					"Field import uid key wasn't found for connector",
					connectorName,
					connectorOperation
			);
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

		JoyceURI schemaUri = JoyceURI.makeNamespaced(JoyceURI.Type.SCHEMA, metadata.getSubtype(), metadata.getNamespace(), metadata.getName());
		enrichedConfig.put(TRANSFORMS, transforms);
		enrichedConfig.put(TRANSFORMS_JOYCE_KEY_UID, connector.getImportKeyUid());
		enrichedConfig.put(TRANSFORMS_JOYCE_KEY_SOURCE, connector.getName());
		enrichedConfig.put(TRANSFORMS_JOYCE_KEY_SCHEMA, schemaUri.toString());
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
			return mapper.readTree(responseBody);

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
			case 3:
				return String.format("http://%s/connectors/%s", kafkaConnectHost, this.computeConnectorName(parts[0], parts[1], parts[2]));
			default:
				return StringUtils.EMPTY;
		}
	}

	private String computeConnectorName(String namespace, String name, String connector) {
		return this.computeConnectorPrefix(namespace, name) + connector;
	}

	private String computeConnectorPrefix(String namespace, String name) {
		return namespace + NAMESPACE_SEPARATOR + name + NAMESPACE_SEPARATOR;
	}
}
