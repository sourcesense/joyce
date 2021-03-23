package com.sourcesense.nile.schemaengine;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
public class SchemaEngineTests {

	protected Path loadResource(String name) throws URISyntaxException {
		URL res =  this.getClass().getClassLoader().getResource(name);
		return Paths.get(res.toURI());
	}


	@Test
	void contextLoads() throws URISyntaxException, IOException {
		String schema = Files.readString(loadResource("schema-01.yaml"));
		String source = Files.readString(loadResource("source-01.json"));
		SchemaEngine schemaEngine = new SchemaEngine();
		JsonNode result = schemaEngine.process(schema, source);
		assertTrue(result.get("name").asText().equals("Leanne Graham"));
	}
}
