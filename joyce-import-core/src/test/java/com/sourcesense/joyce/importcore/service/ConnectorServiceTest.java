package com.sourcesense.joyce.importcore.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.sourcesense.joyce.core.exception.RestException;
import com.sourcesense.joyce.core.exception.SchemaNotFoundException;
import com.sourcesense.joyce.core.model.dto.ConnectorOperationStatus;
import com.sourcesense.joyce.core.model.entity.JoyceSchemaMetadataExtraConnector;
import com.sourcesense.joyce.core.model.entity.SchemaEntity;
import com.sourcesense.joyce.core.model.uri.JoyceSchemaURI;
import com.sourcesense.joyce.core.model.uri.JoyceURIFactory;
import com.sourcesense.joyce.importcore.test.TestUtility;
import com.sourcesense.joyce.schemacore.model.dto.SchemaSave;
import com.sourcesense.joyce.schemacore.service.SchemaService;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.mock.http.client.MockClientHttpResponse;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.RequestMatcher;
import org.springframework.test.web.client.ResponseCreator;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ConnectorServiceTest implements TestUtility {

	private static final String KAFKA_CONNECT_HOST = "test:6682";
	private static final String DOMAIN = "test";
	private static final String PRODUCT = "default";
	private static final String NAME = "user";
	private static final String CONNECTOR = "user-connector";
	private static final String TASK_ID = "0";
	private static final String CONNECTOR_FULL_NAME = String.format("%s:%s:%s:%s", DOMAIN, PRODUCT, NAME, CONNECTOR);
	private static final JoyceSchemaURI JOYCE_SCHEMA_URI = JoyceURIFactory.getInstance().createSchemaURIOrElseThrow(DOMAIN, PRODUCT, NAME);

	@Mock
	private SchemaService schemaService;
	private MockRestServiceServer mockServer;
	private ConnectorService connectorService;

	@BeforeEach
	public void init() {
		RestTemplate restTemplate = new RestTemplate();
		mockServer = MockRestServiceServer.createServer(restTemplate);
		connectorService = new ConnectorService(jsonMapper, restTemplate, schemaService,  KAFKA_CONNECT_HOST);
	}

	@Test
	void shouldGetConnectors() throws IOException, URISyntaxException {
		SchemaEntity schema = this.computeResourceAsObject("schema/save/01.json", SchemaEntity.class);
		when(schemaService.getOrElseThrow(JOYCE_SCHEMA_URI.toString())).thenReturn(schema);

		List<JoyceSchemaMetadataExtraConnector> actual = connectorService.getConnectors(DOMAIN, PRODUCT, NAME);
		List<JoyceSchemaMetadataExtraConnector> expected = this.computeResourceAsObject("connector/saved/01.json", new TypeReference<>() {});

		assertThat(expected).hasSameElementsAs(actual);
	}

	@Test
	void shouldGetConnectorStatus() throws JsonProcessingException {
		String endpoint = this.computeEndpoint(ConnectorEndpoint.CONNECTOR_STATUS);
		JsonNode expected = jsonMapper.createObjectNode().put("status", "OK");
		byte[] responseBody = jsonMapper.writeValueAsBytes(expected);
		this.mockRestCall(endpoint, null, responseBody, HttpMethod.GET, HttpStatus.OK);
		JsonNode actual = connectorService.getConnectorStatus(DOMAIN, PRODUCT, NAME, CONNECTOR);
		assertEquals(expected, actual);
	}

	@Test
	void shouldRestartConnector() {
		String endpoint = this.computeEndpoint(ConnectorEndpoint.RESTART_CONNECTOR);
		this.mockRestCall(endpoint, null, new byte[0], HttpMethod.POST, HttpStatus.NO_CONTENT);
		assertTrue(connectorService.restartConnector(DOMAIN, PRODUCT, NAME, CONNECTOR));
	}

	@Test
	void shouldPauseConnector() {
		String endpoint = this.computeEndpoint(ConnectorEndpoint.PAUSE_CONNECTOR);
		this.mockRestCall(endpoint, null, new byte[0], HttpMethod.PUT, HttpStatus.ACCEPTED);
		assertTrue(connectorService.pauseConnector(DOMAIN, PRODUCT, NAME, CONNECTOR));
	}

	@Test
	void shouldResumeConnector() {
		String endpoint = this.computeEndpoint(ConnectorEndpoint.RESUME_CONNECTOR);
		this.mockRestCall(endpoint, null, new byte[0], HttpMethod.PUT, HttpStatus.ACCEPTED);
		assertTrue(connectorService.resumeConnector(DOMAIN, PRODUCT, NAME, CONNECTOR));
	}

	@Test
	void shouldRestartConnectorTask() {
		String endpoint = this.computeEndpoint(ConnectorEndpoint.RESTART_CONNECTOR_TASK);
		this.mockRestCall(endpoint, null, new byte[0], HttpMethod.POST, HttpStatus.NO_CONTENT);
		assertTrue(connectorService.restartConnectorTask(DOMAIN, PRODUCT, NAME, CONNECTOR, TASK_ID));
	}

	@Test
	void shouldSaveNewConnector() throws IOException, URISyntaxException {
		this.shouldExecuteOperationOnConnector(
				"schema/save/01.json",
				"connector/existing/01.json",
				"connector/save/01.json",
				"connector/status/01.json",
				ConnectorEndpoint.CONNECTORS,
				HttpMethod.POST,
				HttpStatus.CREATED
		);
	}

	@Test
	void shouldUpdateConnector() throws IOException, URISyntaxException {
		this.shouldExecuteOperationOnConnector(
				"schema/save/02.json",
				"connector/existing/02.json",
				"connector/save/02.json",
				"connector/status/02.json",
				ConnectorEndpoint.CONNECTOR_CONFIG,
				HttpMethod.PUT,
				HttpStatus.CREATED
		);
	}

	@Test
	void shouldDeleteConnector() throws IOException, URISyntaxException {
		this.shouldExecuteOperationOnConnector(
				"schema/save/03.json",
				"connector/existing/03.json",
				"connector/save/03.json",
				"connector/status/03.json",
				ConnectorEndpoint.CONNECTOR,
				HttpMethod.DELETE,
				HttpStatus.NO_CONTENT
		);
	}

	@Test
	void shouldDeleteSchemaConnectors() throws IOException, URISyntaxException {

		String operationEndpoint = this.computeEndpoint(ConnectorEndpoint.CONNECTOR);

		SchemaEntity schema = this.computeResourceAsObject("schema/existing/04.json", SchemaEntity.class);
		when(schemaService.getOrElseThrow(JOYCE_SCHEMA_URI.toString())).thenReturn(schema);

		String connectorConfig = this.computeResourceAsString("connector/save/04.json");
		this.mockRestCall(operationEndpoint, connectorConfig, new byte[0], HttpMethod.DELETE, HttpStatus.NO_CONTENT);

		List<ConnectorOperationStatus> expected = this.computeResourceAsObject("connector/status/04.json", new TypeReference<>() {});
		List<ConnectorOperationStatus> actual = connectorService.deleteConnectors(DOMAIN, PRODUCT, NAME);

		assertThat(expected).hasSameElementsAs(actual);
	}

	@Test
	void shouldThrowSchemaNotFoundExceptionWhenMissingSchema() {
		when(schemaService.getOrElseThrow(JOYCE_SCHEMA_URI.toString())).thenThrow(SchemaNotFoundException.class);
		assertThrows(
				SchemaNotFoundException.class,
				() -> connectorService.getConnectors(DOMAIN, PRODUCT, NAME)
		);
	}

	@Test
	void shouldNotGetConnectorStatusAndThrowRestException() {
		this.shouldThrowsRestException(
				ConnectorEndpoint.CONNECTOR_STATUS,
				HttpMethod.GET,
				() -> connectorService.getConnectorStatus(DOMAIN, PRODUCT, NAME, CONNECTOR)
		);
	}

	@Test
	void shouldNotRestartConnectorAndThrowRestException() {
		this.shouldThrowsRestException(
				ConnectorEndpoint.RESTART_CONNECTOR,
				HttpMethod.POST,
				() -> connectorService.restartConnector(DOMAIN, PRODUCT, NAME, CONNECTOR)
		);
	}

	@Test
	void shouldNotPauseConnectorAndThrowRestException() {
		this.shouldThrowsRestException(
				ConnectorEndpoint.PAUSE_CONNECTOR,
				HttpMethod.PUT,
				() -> connectorService.pauseConnector(DOMAIN, PRODUCT, NAME, CONNECTOR)
		);
	}

	@Test
	void shouldNotResumeConnectorAndThrowRestException() {
		this.shouldThrowsRestException(
				ConnectorEndpoint.RESUME_CONNECTOR,
				HttpMethod.PUT,
				() -> connectorService.resumeConnector(DOMAIN, PRODUCT, NAME, CONNECTOR)
		);
	}

	@Test
	void shouldNotRestartConnectorTaskAndThrowRestException() {
		this.shouldThrowsRestException(
				ConnectorEndpoint.RESTART_CONNECTOR_TASK,
				HttpMethod.POST,
				() -> connectorService.restartConnectorTask(DOMAIN, PRODUCT, NAME, CONNECTOR, TASK_ID)
		);
	}

	private void shouldExecuteOperationOnConnector(
			String schemaPath,
			String existingConnectorPath,
			String newConnectorPath,
			String statusPath,
			ConnectorEndpoint opEndpoint,
			HttpMethod operationMethod,
			HttpStatus operationStatus) throws IOException, URISyntaxException {

		String existingConnectorsEndpoint = this.computeEndpoint(ConnectorEndpoint.CONNECTORS);
		String operationEndpoint = this.computeEndpoint(opEndpoint);

		SchemaSave schema = this.computeResourceAsObject(schemaPath, SchemaSave.class);

		byte[] existingConnectors = this.computeResourceAsByteArray(existingConnectorPath);
		this.mockRestCall(existingConnectorsEndpoint, null, existingConnectors, HttpMethod.GET, HttpStatus.OK);

		String connectorConfig = this.computeResourceAsString(newConnectorPath);
		this.mockRestCall(operationEndpoint, connectorConfig, new byte[0], operationMethod, operationStatus);

		List<ConnectorOperationStatus> expected = this.computeResourceAsObject(statusPath, new TypeReference<>() {});
		List<ConnectorOperationStatus> actual = connectorService.computeConnectors(schema);

		assertThat(expected).hasSameElementsAs(actual);
	}

	private void shouldThrowsRestException(
			ConnectorEndpoint connectorEndpoint,
			HttpMethod method,
			Executable executable) {

		String endpoint = this.computeEndpoint(connectorEndpoint);
		this.mockRestCall(endpoint, null, new byte[0], method, HttpStatus.INTERNAL_SERVER_ERROR);
		assertThrows(RestException.class, executable);
	}

	private void mockRestCall(
			String endpoint,
			String requestBody,
			byte[] responseBody,
			HttpMethod method,
			HttpStatus status) {

		mockServer.expect(this.computeMockedRequest(endpoint, requestBody, method))
				.andRespond(this.computeMockedResponse(responseBody, status));
	}

	private RequestMatcher computeMockedRequest(String endpoint, String requestBody, HttpMethod method) {
		return request -> {
			assertEquals(request.getURI().toString(), endpoint);
			assertEquals(request.getMethod(), method);
			if (Objects.nonNull(requestBody)) {
				assertEquals(
						jsonMapper.readTree(requestBody),
						jsonMapper.readTree(request.getBody().toString())
				);
			}
		};
	}

	private ResponseCreator computeMockedResponse(byte[] responseBody, HttpStatus status) {
		return request -> {
			ClientHttpResponse response = new MockClientHttpResponse(responseBody, status);
			response.getHeaders().put(HttpHeaders.CONTENT_TYPE, Collections.singletonList(MediaType.APPLICATION_JSON_VALUE));
			return response;
		};
	}

	private String computeEndpoint(ConnectorEndpoint endpoint) {
		switch (endpoint) {
			case CONNECTORS:
				return String.format("http://%s/connectors", KAFKA_CONNECT_HOST);
			case CONNECTOR:
				return String.format("http://%s/connectors/%s", KAFKA_CONNECT_HOST, CONNECTOR_FULL_NAME);
			case CONNECTOR_CONFIG:
				return String.format("http://%s/connectors/%s/config", KAFKA_CONNECT_HOST, CONNECTOR_FULL_NAME);
			case CONNECTOR_STATUS:
				return String.format("http://%s/connectors/%s/status", KAFKA_CONNECT_HOST, CONNECTOR_FULL_NAME);
			case RESTART_CONNECTOR:
				return String.format("http://%s/connectors/%s/restart", KAFKA_CONNECT_HOST, CONNECTOR_FULL_NAME);
			case PAUSE_CONNECTOR:
				return String.format("http://%s/connectors/%s/pause", KAFKA_CONNECT_HOST, CONNECTOR_FULL_NAME);
			case RESUME_CONNECTOR:
				return String.format("http://%s/connectors/%s/resume", KAFKA_CONNECT_HOST, CONNECTOR_FULL_NAME);
			case RESTART_CONNECTOR_TASK:
				return String.format("http://%s/connectors/%s/tasks/%s/restart", KAFKA_CONNECT_HOST, CONNECTOR_FULL_NAME, TASK_ID);
			default:
				return StringUtils.EMPTY;
		}
	}

	public enum ConnectorEndpoint {
		CONNECTORS,
		CONNECTOR,
		CONNECTOR_CONFIG,
		CONNECTOR_STATUS,
		RESTART_CONNECTOR,
		PAUSE_CONNECTOR,
		RESUME_CONNECTOR,
		RESTART_CONNECTOR_TASK
	}
}
