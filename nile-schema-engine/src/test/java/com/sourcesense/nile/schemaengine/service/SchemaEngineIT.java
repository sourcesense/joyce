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
		String schema = Files.readString(Path.of(resourceLoader.getResource("schema/11.yaml").getURI()));
		String source = Files.readString(Path.of(resourceLoader.getResource("source/10.json").getURI()));
		JsonNode result = schemaEngine.process(schema, source).getJson();
		Assertions.assertEquals("Leanne Graham", result.get("name").asText());
		Assertions.assertEquals("Sincere@april.biz", result.get("mail").asText());
		Assertions.assertEquals("bar", result.get("foo").asText());
	}

	@Test
	public void schemaParsing10() throws IOException, URISyntaxException {
		String schema = Files.readString(Path.of(resourceLoader.getResource("schema/10.yaml").getURI()));
		String source = Files.readString(Path.of(resourceLoader.getResource("source/10.json").getURI()));
		ProcessResult result = schemaEngine.process(schema, source);
		Assertions.assertEquals("Leanne Graham", result.getJson().get("name").asText());
		Assertions.assertEquals("Sincere@april.biz", result.getJson().get("mail").asText());
		Assertions.assertEquals("Gwenborough, Kulas Light", result.getJson().get("address").asText());
		Assertions.assertEquals("nile://oracle/users/1", result.getContext().get().get("message_key"));
		Assertions.assertEquals("users", result.getContext().get().get("collection"));
		Assertions.assertEquals("simpleUser", result.getJson().get("docType").asText());
	}

	@SpringBootApplication
	static class TestConfiguration {
	}

}
