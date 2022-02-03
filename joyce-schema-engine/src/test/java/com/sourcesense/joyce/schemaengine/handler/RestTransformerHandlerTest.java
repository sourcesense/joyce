package com.sourcesense.joyce.schemaengine.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.MustacheFactory;
import com.sourcesense.joyce.schemaengine.test.TestUtility;
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

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class RestTransformerHandlerTest implements TestUtility {

	private MockRestServiceServer server;
	private RestTransformerHandler restTransformerHandler;


	@BeforeEach
	void init() {
		RestTemplate restTemplate = new RestTemplate();
		server = MockRestServiceServer.createServer(restTemplate);
		MustacheFactory mustacheFactory = new DefaultMustacheFactory();
		restTransformerHandler = new RestTransformerHandler(this.initJsonMapper(), restTemplate, mustacheFactory);
		restTransformerHandler.configure();
	}

	@Test
	void shouldExecuteGetRequestWithoutParams() throws IOException, URISyntaxException {
		server.expect(request -> {
			assertEquals(request.getURI().toString(), "http://test:8080/posts/1");
			assertEquals(request.getMethod(), HttpMethod.GET);

		}).andRespond(withSuccess(
				this.getResourceAsString("rest/response/41.json"),
				MediaType.APPLICATION_JSON
		));

		this.shouldProcessScript(
				"getWithoutParams",
				"source/10.json",
				"rest/request/41.json",
				"result/41.json"
		);

	}

	@Test
	void shouldExecuteGetRequestWithParams() throws IOException, URISyntaxException {
		server.expect(request -> {
			assertEquals(request.getURI().toString(), "http://test:8080/posts/Bret");
			assertEquals(request.getMethod(), HttpMethod.GET);

		}).andRespond(withSuccess(
				this.getResourceAsString("rest/response/42.json"),
				MediaType.APPLICATION_JSON
		));

		this.shouldProcessScript(
				"getWithParams",
				"source/10.json",
				"rest/request/42.json",
				"result/42.json"
		);

	}

	@Test
	void shouldReturnFullResponseIfNoExtract() throws IOException, URISyntaxException {
		server.expect(request -> {
			assertEquals(request.getURI().toString(), "http://test:8080/posts/1");
			assertEquals(request.getMethod(), HttpMethod.GET);

		}).andRespond(withSuccess(
				this.getResourceAsString("rest/response/43.json"),
				MediaType.APPLICATION_JSON
		));

		this.shouldProcessScript(
				"noExtract",
				"source/10.json",
				"rest/request/43.json",
				"result/43.json"
		);
	}

	@Test
	void shouldExecutePostRequestWithBody() throws IOException, URISyntaxException {
		server.expect(request -> {
			assertEquals(request.getURI().toString(), "http://test:8080/posts");
			assertEquals(request.getMethod(), HttpMethod.POST);
			assertEquals(request.getBody().toString(), "{\"content\": \"Bret\"}");

		}).andRespond(withSuccess(
				this.getResourceAsString("rest/response/44.json"),
				MediaType.APPLICATION_JSON
		));

		this.shouldProcessScript(
				"postWithBody",
				"source/10.json",
				"rest/request/44.json",
				"result/44.json"
		);
	}

	@Test
	void shouldExecuteRequestWithMultiValueHeaders() throws IOException, URISyntaxException {
		List<String> testHeaders = Arrays.asList("value1", "valueN", "Bret");

		server.expect(request -> {
			assertEquals(request.getURI().toString(), "http://test:8080/posts/1");
			assertEquals(request.getMethod(), HttpMethod.GET);
			assertTrue(testHeaders.equals(request.getHeaders().get("test")));

		}).andRespond(withSuccess(
				this.getResourceAsString("rest/response/45.json"),
				MediaType.APPLICATION_JSON
		));

		this.shouldProcessScript(
				"postWithBody",
				"source/10.json",
				"rest/request/45.json",
				"result/45.json"
		);

	}

	private void shouldProcessScript(
			String key,
			String sourcePath,
			String valuePath,
			String resultPath) throws IOException, URISyntaxException {

		JsonNode value = this.getResourceAsNode(valuePath);
		JsonNode source = this.getResourceAsNode(sourcePath);
		assertEquals(
				this.getResourceAsNode(resultPath).asText(),
				restTransformerHandler.process(key, "string", value, source, Optional.empty(), Optional.empty()).asText()
		);
	}
}
