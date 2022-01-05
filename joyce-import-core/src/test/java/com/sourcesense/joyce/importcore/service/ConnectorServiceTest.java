package com.sourcesense.joyce.importcore.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.sourcesense.joyce.core.dao.SchemaDao;
import com.sourcesense.joyce.core.dto.ConnectorOperationStatus;
import com.sourcesense.joyce.core.dto.SchemaSave;
import com.sourcesense.joyce.core.exception.RestException;
import com.sourcesense.joyce.core.exception.SchemaNotFoundException;
import com.sourcesense.joyce.core.model.JoyceSchemaMetadataExtraConnector;
import com.sourcesense.joyce.core.model.JoyceURI;
import com.sourcesense.joyce.core.model.SchemaEntity;
import com.sourcesense.joyce.core.service.SchemaService;
import com.sourcesense.joyce.importcore.exception.ImportException;
import io.netty.util.internal.StringUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
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
import com.sourcesense.joyce.importcore.UtilitySupplier;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ConnectorServiceTest implements UtilitySupplier {

	private static final String KAFKA_CONNECT_HOST = "test:6682";
	private static final String SUBTYPE = "import";
	private static final String NAMESPACE = "test";
	private static final String NAME = "schema";
	private static final String CONNECTOR = "connector";
	private static final String TASK_ID = "0";
	private static final String NAMESPACED_CONNECTOR = computeNamespacedConnector();
	private static final String JOYCE_SCHEMA_URI = computeSchemaUri();

	private MockRestServiceServer mockServer;
	private SchemaDao schemaDao;
	private ConnectorService connectorService;

	@BeforeEach
	public void init() throws NoSuchFieldException, IllegalAccessException {
		schemaDao = mock(SchemaDao.class);
		SchemaService schemaService = new SchemaService(schemaDao, null, null, null);

		RestTemplate restTemplate = new RestTemplate();
		mockServer = MockRestServiceServer.createServer(restTemplate);

		connectorService = new ConnectorService(mapper, restTemplate, schemaService);

		Field restEndpoint = connectorService.getClass().getDeclaredField("kafkaConnectHost");
		restEndpoint.setAccessible(true);
		restEndpoint.set(connectorService, KAFKA_CONNECT_HOST);
	}

	@Test
	void shouldGetConnectors() throws IOException, URISyntaxException {
		SchemaEntity schema = this.computeResourceAsObject("schema/save/01.json", SchemaEntity.class);
		when(schemaDao.get(JOYCE_SCHEMA_URI)).thenReturn(Optional.of(schema));

		List<JoyceSchemaMetadataExtraConnector> actual = connectorService.getConnectors(SUBTYPE, NAMESPACE, NAME);
		List<JoyceSchemaMetadataExtraConnector> expected = this.computeResourceAsObject("connector/saved/01.json", new TypeReference<>() {
		});

		assertThat(expected).hasSameElementsAs(actual);
	}

	@Test
	void shouldGetConnectorStatus() throws JsonProcessingException {
		String endpoint = this.computeEndpoint(ConnectorEndpoint.CONNECTOR_STATUS);
		JsonNode expected = mapper.createObjectNode().put("status", "OK");
		byte[] responseBody = mapper.writeValueAsBytes(expected);
		this.mockRestCall(endpoint, null, responseBody, HttpMethod.GET, HttpStatus.OK);
		JsonNode actual = connectorService.getConnectorStatus(NAMESPACE, NAME, CONNECTOR);
		assertEquals(expected, actual);
	}

	@Test
	void shouldRestartConnector() {
		String endpoint = this.computeEndpoint(ConnectorEndpoint.RESTART_CONNECTOR);
		this.mockRestCall(endpoint, null, new byte[0], HttpMethod.POST, HttpStatus.NO_CONTENT);
		assertTrue(connectorService.restartConnector(NAMESPACE, NAME, CONNECTOR));
	}

	@Test
	void shouldPauseConnector() {
		String endpoint = this.computeEndpoint(ConnectorEndpoint.PAUSE_CONNECTOR);
		this.mockRestCall(endpoint, null, new byte[0], HttpMethod.PUT, HttpStatus.ACCEPTED);
		assertTrue(connectorService.pauseConnector(NAMESPACE, NAME, CONNECTOR));
	}

	@Test
	void shouldResumeConnector() {
		String endpoint = this.computeEndpoint(ConnectorEndpoint.RESUME_CONNECTOR);
		this.mockRestCall(endpoint, null, new byte[0], HttpMethod.PUT, HttpStatus.ACCEPTED);
		assertTrue(connectorService.resumeConnector(NAMESPACE, NAME, CONNECTOR));
	}

	@Test
	void shouldRestartConnectorTask() {
		String endpoint = this.computeEndpoint(ConnectorEndpoint.RESTART_CONNECTOR_TASK);
		this.mockRestCall(endpoint, null, new byte[0], HttpMethod.POST, HttpStatus.NO_CONTENT);
		assertTrue(connectorService.restartConnectorTask(NAMESPACE, NAME, CONNECTOR, TASK_ID));
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
		when(schemaDao.get(JOYCE_SCHEMA_URI)).thenReturn(Optional.of(schema));

		String connectorConfig = this.computeResourceAsString("connector/save/04.json");
		this.mockRestCall(operationEndpoint, connectorConfig, new byte[0], HttpMethod.DELETE, HttpStatus.NO_CONTENT);

		List<ConnectorOperationStatus> expected = this.computeResourceAsObject("connector/status/04.json", new TypeReference<>() {});
		List<ConnectorOperationStatus> actual = connectorService.deleteConnectors(SUBTYPE, NAMESPACE, NAME);

		assertThat(expected).hasSameElementsAs(actual);
	}

	@Test
	void shouldThrowImportExceptionWhenWrongSubtype() {
		assertThrows(
				ImportException.class,
				() -> connectorService.getConnectors("wrong subtype", NAMESPACE, NAME)
		);
	}

	@Test
	void shouldThrowSchemaNotFoundExceptionWhenMissingSchema() {
		when(schemaDao.get(JOYCE_SCHEMA_URI)).thenReturn(Optional.empty());
		assertThrows(
				SchemaNotFoundException.class,
				() -> connectorService.getConnectors(SUBTYPE, NAMESPACE, NAME)
		);
	}

	@Test
	void shouldNotGetConnectorStatusAndThrowRestException() {
		this.shouldThrowsRestException(
				ConnectorEndpoint.CONNECTOR_STATUS,
				HttpMethod.GET,
				() -> connectorService.getConnectorStatus(NAMESPACE, NAME, CONNECTOR)
		);
	}

	@Test
	void shouldNotRestartConnectorAndThrowRestException() {
		this.shouldThrowsRestException(
				ConnectorEndpoint.RESTART_CONNECTOR,
				HttpMethod.POST,
				() -> connectorService.restartConnector(NAMESPACE, NAME, CONNECTOR)
		);
	}

	@Test
	void shouldNotPauseConnectorAndThrowRestException() {
		this.shouldThrowsRestException(
				ConnectorEndpoint.PAUSE_CONNECTOR,
				HttpMethod.PUT,
				() -> connectorService.pauseConnector(NAMESPACE, NAME, CONNECTOR)
		);
	}

	@Test
	void shouldNotResumeConnectorAndThrowRestException() {
		this.shouldThrowsRestException(
				ConnectorEndpoint.RESUME_CONNECTOR,
				HttpMethod.PUT,
				() -> connectorService.resumeConnector(NAMESPACE, NAME, CONNECTOR)
		);
	}

	@Test
	void shouldNotRestartConnectorTaskAndThrowRestException() {
		this.shouldThrowsRestException(
				ConnectorEndpoint.RESTART_CONNECTOR_TASK,
				HttpMethod.POST,
				() -> connectorService.restartConnectorTask(NAMESPACE, NAME, CONNECTOR, TASK_ID)
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
						mapper.readTree(requestBody),
						mapper.readTree(request.getBody().toString())
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
				return String.format("http://%s/connectors/%s", KAFKA_CONNECT_HOST, NAMESPACED_CONNECTOR);
			case CONNECTOR_CONFIG:
				return String.format("http://%s/connectors/%s/config", KAFKA_CONNECT_HOST, NAMESPACED_CONNECTOR);
			case CONNECTOR_STATUS:
				return String.format("http://%s/connectors/%s/status", KAFKA_CONNECT_HOST, NAMESPACED_CONNECTOR);
			case RESTART_CONNECTOR:
				return String.format("http://%s/connectors/%s/restart", KAFKA_CONNECT_HOST, NAMESPACED_CONNECTOR);
			case PAUSE_CONNECTOR:
				return String.format("http://%s/connectors/%s/pause", KAFKA_CONNECT_HOST, NAMESPACED_CONNECTOR);
			case RESUME_CONNECTOR:
				return String.format("http://%s/connectors/%s/resume", KAFKA_CONNECT_HOST, NAMESPACED_CONNECTOR);
			case RESTART_CONNECTOR_TASK:
				return String.format("http://%s/connectors/%s/tasks/%s/restart", KAFKA_CONNECT_HOST, NAMESPACED_CONNECTOR, TASK_ID);
			default:
				return StringUtil.EMPTY_STRING;
		}
	}

	private static String computeNamespacedConnector() {
		return NAMESPACE + JoyceURI.NAMESPACE_SEPARATOR + NAME + JoyceURI.NAMESPACE_SEPARATOR + CONNECTOR;
	}

	private static String computeSchemaUri() {
		return JoyceURI.makeNamespaced(JoyceURI.Type.SCHEMA, JoyceURI.Subtype.IMPORT, NAMESPACE, NAME).toString();
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
