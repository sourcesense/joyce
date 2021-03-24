package com.sourcesense.nile.schemaengine.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.sourcesense.nile.schemaengine.handlers.JsonPathTransformerHandler;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.context.ContextConfiguration;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@SpringBootTest(classes = {SchemaEngine.class, SchemaEngineProperties.class, JsonPathTransformerHandler.class})
@ContextConfiguration
public class SchemaEngineIT {
	@Autowired
	private SchemaEngine schemaEngine;

	@Value("classpath:schema-01.yaml")
	Resource schemaResource;

	@Value("classpath:source-01.json")
	Resource sourceResource;

	@Test
	public void schemaParsing01() throws IOException, URISyntaxException {
		String schema = Files.readString(Path.of(schemaResource.getURI()));
		String source = Files.readString(Path.of(sourceResource.getURI()));
		JsonNode result = schemaEngine.process(schema, source);

		Assertions.assertEquals("Leanne Graham", result.get("name").asText());
		Assertions.assertEquals("Sincere@april.biz", result.get("mail").asText());
		Assertions.assertEquals("Gwenborough, Kulas Light", result.get("address").asText());
	}

	@SpringBootApplication
	static class TestConfiguration {
	}

}
