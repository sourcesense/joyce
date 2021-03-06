//package com.sourcesense.joyce.schemacore.dao;
//
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.core.type.TypeReference;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.sourcesense.joyce.core.configuration.SchemaServiceProperties;
//import com.sourcesense.joyce.core.exception.RestException;
//import com.sourcesense.joyce.core.exception.handler.CustomExceptionHandler;
//import com.sourcesense.joyce.core.model.JoyceSchemaMetadata;
//import com.sourcesense.joyce.core.model.JoyceURI;
//import com.sourcesense.joyce.core.model.SchemaEntity;
//import com.sourcesense.joyce.schemacore.mapper.SchemaMapper;
//import com.sourcesense.joyce.schemacore.mapper.SchemaMapperImpl;
//import com.sourcesense.joyce.schemacore.test.TestUtility;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.mockito.junit.jupiter.MockitoSettings;
//import org.mockito.quality.Strictness;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.HttpMethod;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.MediaType;
//import org.springframework.http.client.ClientHttpResponse;
//import org.springframework.mock.http.client.MockClientHttpResponse;
//import org.springframework.test.web.client.MockRestServiceServer;
//import org.springframework.test.web.client.RequestMatcher;
//import org.springframework.test.web.client.ResponseCreator;
//import org.springframework.web.client.RestTemplate;
//
//import java.io.IOException;
//import java.util.Collections;
//import java.util.List;
//import java.util.Objects;
//import java.util.Optional;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.junit.jupiter.api.Assertions.*;
//
//Todo: will be removed after refactor
//@ExtendWith(MockitoExtension.class)
//@MockitoSettings(strictness = Strictness.LENIENT)
//public class RestSchemaDaoTest implements TestUtility {
//
//	private static final String REST_ENDPOINT = "http://test:6651";
//	private static final String SUBTYPE = "import";
//	private static final String NAMESPACE = "test";
//	private static final String NAME = "object";
//
//	private ObjectMapper mapper;
//	private RestSchemaDao restSchemaDao;
//	private MockRestServiceServer mockServer;
//
//	@BeforeEach
//	public void init() {
//		RestTemplate restTemplate = new RestTemplate();
//		SchemaMapper schemaMapper = new SchemaMapperImpl();
//		CustomExceptionHandler customExceptionHandler = new CustomExceptionHandler();
//		mapper = this.initJsonMapper();
//		mockServer = MockRestServiceServer.createServer(restTemplate);
//		restSchemaDao = this.initRestSchemaDao(restTemplate, schemaMapper, customExceptionHandler);
//	}
//
//	@Test
//	void getMethodShouldReturnSchemaEntityOnSuccessfulRestCall() throws IOException {
//		String endpoint = String.format("%s/api/schema/%s/%s/%s", REST_ENDPOINT, SUBTYPE, NAMESPACE, NAME);
//		byte[] responseBody = this.getResourceAsBytes("schema/01.json");
//
//		this.mockRestCall(endpoint, null, responseBody, HttpMethod.GET, HttpStatus.OK);
//
//		String schemaUri = String.format("joyce://schema/%s/%s.%s", SUBTYPE, NAMESPACE, NAME);
//		SchemaEntity actual = restSchemaDao.get(schemaUri).orElse(null);
//		SchemaEntity expected = mapper.readValue(responseBody, SchemaEntity.class);
//
//		assertEquals(expected, actual);
//	}
//
//
//	@Test
//	void getMethodShouldReturnEmptyOptionalOnFailedRestCall() {
//		String endpoint = String.format("%s/api/schema/%s/%s/%s", REST_ENDPOINT, SUBTYPE, NAMESPACE, NAME);
//
//		this.mockRestCall(endpoint, null, new byte[0], HttpMethod.GET, HttpStatus.INTERNAL_SERVER_ERROR);
//
//		String schemaUri = String.format("joyce://schema/%s/%s.%s", SUBTYPE, NAMESPACE, NAME);
//		Optional<SchemaEntity> actual = restSchemaDao.get(schemaUri);
//		Optional<SchemaEntity> expected = Optional.empty();
//
//		assertEquals(expected, actual);
//	}
//
//	@Test
//	void getAllMethodShouldReturnSchemaEntityListOnSuccessfulRestCall() throws IOException {
//		String endpoint = String.format("%s/api/schema?root_only=false&full_schema=true", REST_ENDPOINT);
//		byte[] responseBody = this.getResourceAsBytes("schema/02.json");
//
//		this.mockRestCall(endpoint, null, responseBody, HttpMethod.GET, HttpStatus.OK);
//
//		List<SchemaEntity> actual = restSchemaDao.getAll(false);
//		List<SchemaEntity> expected = mapper.readValue(responseBody, new TypeReference<>() {});
//
//		assertThat(expected).hasSameElementsAs(actual);
//	}
//
//	@Test
//	void getAllMethodShouldThrowOnFailedRestCall() {
//		String endpoint = String.format("%s/api/schema?root_only=false&full_schema=true", REST_ENDPOINT);
//
//		this.mockRestCall(endpoint, null, new byte[0], HttpMethod.GET, HttpStatus.INTERNAL_SERVER_ERROR);
//
//		assertThrows(
//				RestException.class,
//				() -> restSchemaDao.getAll(false)
//		);
//	}
//
//	@Test
//	void getAllBySubtypeAndNamespaceMethodShouldReturnSchemaEntityListOnSuccessfulRestCall() throws IOException {
//		String endpoint = String.format("%s/api/schema/%s/%s?root_only=false&full_schema=true", REST_ENDPOINT, SUBTYPE, NAMESPACE);
//		byte[] responseBody = this.getResourceAsBytes("schema/02.json");
//
//		this.mockRestCall(endpoint, null, responseBody, HttpMethod.GET, HttpStatus.OK);
//
//		List<SchemaEntity> actual = restSchemaDao.getAllBySubtypeAndNamespace(JoyceURI.Subtype.IMPORT, NAMESPACE, false);
//		List<SchemaEntity> expected = mapper.readValue(responseBody, new TypeReference<>() {});
//
//		assertThat(expected).hasSameElementsAs(actual);
//	}
//
//	@Test
//	void saveMethodShouldFinish() throws JsonProcessingException {
//		String endpoint = String.format("%s/api/schema", REST_ENDPOINT);
//		String requestBody = this.getResourceAsString("schema/03.json");
//		SchemaEntity schema = mapper.readValue(requestBody, SchemaEntity.class);
//
//		this.mockRestCall(endpoint, requestBody, new byte[0], HttpMethod.POST, HttpStatus.CREATED);
//		restSchemaDao.save(schema);
//
//		assertTrue(true);
//	}
//
//
//	@Test
//	void saveMethodShouldThrowOnFailedRestCall() throws JsonProcessingException {
//		String endpoint = String.format("%s/api/schema", REST_ENDPOINT);
//		String requestBody = this.getResourceAsString("schema/03.json");
//		SchemaEntity schema = mapper.readValue(requestBody, SchemaEntity.class);
//
//		this.mockRestCall(endpoint, requestBody, new byte[0], HttpMethod.POST, HttpStatus.INTERNAL_SERVER_ERROR);
//
//		assertThrows(
//				RestException.class,
//				() -> restSchemaDao.save(schema)
//		);
//	}
//
//	@Test
//	void getAllBySubtypeAndNamespaceMethodShouldThrowOnFailedRestCall() {
//		String endpoint = String.format("%s/api/schema/%s/%s?root_only=false&full_schema=true", REST_ENDPOINT, SUBTYPE, NAMESPACE);
//
//		this.mockRestCall(endpoint, null, new byte[0], HttpMethod.GET, HttpStatus.INTERNAL_SERVER_ERROR);
//
//		assertThrows(
//				RestException.class,
//				() -> restSchemaDao.getAllBySubtypeAndNamespace(JoyceURI.Subtype.IMPORT, NAMESPACE, false)
//		);
//	}
//
//	@Test
//	void deleteMethodShouldFinish() {
//		String endpoint = String.format("%s/api/schema/%s/%s/%s", REST_ENDPOINT, SUBTYPE, NAMESPACE, NAME);
//
//		this.mockRestCall(endpoint, null, new byte[0], HttpMethod.DELETE, HttpStatus.OK);
//
//		JoyceSchemaMetadata metadata = new JoyceSchemaMetadata();
//		metadata.setSubtype(JoyceURI.Subtype.IMPORT);
//		metadata.setNamespace(NAMESPACE);
//		metadata.setName(NAME);
//		SchemaEntity schema = new SchemaEntity();
//		schema.setMetadata(metadata);
//
//		restSchemaDao.delete(schema);
//
//		assertTrue(true);
//	}
//
//	private void mockRestCall(
//			String endpoint,
//			String requestBody,
//			byte[] responseBody,
//			HttpMethod method,
//			HttpStatus status) {
//
//		mockServer.expect(this.computeMockedRequest(endpoint, requestBody, method))
//				.andRespond(this.computeMockedResponse(responseBody, status));
//	}
//
//	private RequestMatcher computeMockedRequest(String endpoint, String requestBody, HttpMethod method) {
//		return request -> {
//			assertEquals(request.getURI().toString(), endpoint);
//			assertEquals(request.getMethod(), method);
//			if (Objects.nonNull(requestBody)) {
//				SchemaEntity expected = mapper.readValue(requestBody, SchemaEntity.class);
//				SchemaEntity actual = mapper.readValue(request.getBody().toString(), SchemaEntity.class);
//				assertEquals(expected, actual);
//			}
//		};
//	}
//
//	private ResponseCreator computeMockedResponse(byte[] responseBody, HttpStatus status) {
//		return request -> {
//			ClientHttpResponse response = new MockClientHttpResponse(responseBody, status);
//			response.getHeaders().put(HttpHeaders.CONTENT_TYPE, Collections.singletonList(MediaType.APPLICATION_JSON_VALUE));
//			return response;
//		};
//	}
//
//	private RestSchemaDao initRestSchemaDao(RestTemplate restTemplate, SchemaMapper schemaMapper, CustomExceptionHandler customExceptionHandler) {
//		SchemaServiceProperties props = new SchemaServiceProperties();
//		props.setRestEndpoint(REST_ENDPOINT);
//		return new RestSchemaDao(
//				restTemplate,
//				schemaMapper,
//				customExceptionHandler,
//				props
//		);
//	}
//}
