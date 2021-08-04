package com.sourcesense.joyce.core.dao;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sourcesense.joyce.core.exception.handler.CustomExceptionHandler;
import com.sourcesense.joyce.core.mapper.SchemaMapper;
import com.sourcesense.joyce.core.mapper.SchemaMapperImpl;
import com.sourcesense.joyce.core.utility.UtilitySupplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class RestSchemaDaoTest implements UtilitySupplier {

	private static final String SCHEMA_URI = "joyce://schema/import/test.object";

	private ObjectMapper mapper;
	private RestSchemaDao restSchemaDao;
	private MockRestServiceServer mockServer;

	@BeforeEach
	public void init() {
		RestTemplate restTemplate = new RestTemplate();
		SchemaMapper schemaMapper = new SchemaMapperImpl();
		CustomExceptionHandler customExceptionHandler = new CustomExceptionHandler();
		mapper = this.initJsonMapper();
		mockServer = MockRestServiceServer.createServer(restTemplate);
		restSchemaDao = new RestSchemaDao(mapper, restTemplate, schemaMapper, customExceptionHandler);
	}

	@Test
	void getMethodShouldReturnSchemaEntity() {

	}

//	private void ciao() {
//		mockServer.expect(request -> {
//			assertEquals(request.getURI().toString(), "http://test:8080/posts?test=pv1&test=pvN");
//			assertEquals(request.getMethod(), HttpMethod.POST);
//			assertEquals(request.getBody().toString(), "{\n \"content\": \"test\"\n}\n");
//			assertThat(testHeaders.equals(request.getHeaders().get("test")));
//
//		}).andRespond(withSuccess(
//				this.getResourceAsString("rest/response/35.json"),
//				MediaType.APPLICATION_JSON
//		));
//	}
}
