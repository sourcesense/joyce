package com.sourcesense.nile.schemaengine.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sourcesense.nile.schemaengine.TestApplication;
import com.sourcesense.nile.schemaengine.dto.ProcessResult;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;

@SpringBootTest(classes = TestApplication.class)
@ActiveProfiles({"default", "test"})
@ExtendWith(MockitoExtension.class)
public class SchemaEngineIT {
	private ObjectMapper mapper = new ObjectMapper();

	@Autowired
	private SchemaEngine schemaEngine;

	@Autowired
	private ResourceLoader resourceLoader;


	@Test
	public void loadHandlerFromApplciationYamlShouldWork() throws IOException, URISyntaxException {
		String schema = Files.readString(Path.of(resourceLoader.getResource("schema/11.json").getURI()));
		String source = Files.readString(Path.of(resourceLoader.getResource("source/10.json").getURI()));
		JsonNode result = schemaEngine.process(schema, source).getJson();
		Assertions.assertEquals("Leanne Graham", result.get("name").asText());
		Assertions.assertEquals("Sincere@april.biz", result.get("mail").asText());
		Assertions.assertEquals("bar", result.get("foo").asText());
	}

	@Test
	public void schemaParsing10() throws IOException, URISyntaxException {
		String schema = Files.readString(Path.of(resourceLoader.getResource("schema/10.json").getURI()));
		String source = Files.readString(Path.of(resourceLoader.getResource("source/10.json").getURI()));
		ProcessResult result = schemaEngine.process(schema, source);
		Assertions.assertEquals("Leanne Graham", result.getJson().get("name").asText());
		Assertions.assertEquals("Sincere@april.biz", result.getJson().get("mail").asText());
		Assertions.assertEquals("Gwenborough, Kulas Light", result.getJson().get("address").asText());

		Assertions.assertEquals("users", result.getMetadata().get().get("collection").asText());
		Assertions.assertEquals("simpleUser", result.getJson().get("docType").asText());

	}

	@Test
	public void schemaParsing16() throws IOException, URISyntaxException {
		String schema = Files.readString(Path.of(resourceLoader.getResource("schema/30.json").getURI()));
		String source = Files.readString(Path.of(resourceLoader.getResource("source/16.json").getURI()));
		ProcessResult result = schemaEngine.process(schema, source);
		Assertions.assertEquals("Leanne Graham", result.getJson().get("id").asText());
		Assertions.assertEquals("1", result.getJson().get("more-id").asText());
		Assertions.assertEquals("Mario", result.getJson().get("utenti").get(0).get("name").asText());

	}

	@SpringBootApplication
	static class TestConfiguration {
	}

}
