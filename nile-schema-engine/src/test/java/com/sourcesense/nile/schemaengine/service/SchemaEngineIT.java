/*
 *  Copyright 2021 Sourcesense Spa
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sourcesense.nile.schemaengine.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sourcesense.nile.schemaengine.TestApplication;
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
		JsonNode result = schemaEngine.process(schema, source);
		Assertions.assertEquals("Leanne Graham", result.get("name").asText());
		Assertions.assertEquals("Sincere@april.biz", result.get("mail").asText());
		Assertions.assertEquals("bar", result.get("foo").asText());
	}

	@Test
	public void schemaParsing10() throws IOException, URISyntaxException {
		String schema = Files.readString(Path.of(resourceLoader.getResource("schema/10.json").getURI()));
		String source = Files.readString(Path.of(resourceLoader.getResource("source/10.json").getURI()));
		JsonNode result = schemaEngine.process(schema, source);
		Assertions.assertEquals("Leanne Graham", result.get("name").asText());
		Assertions.assertEquals("Sincere@april.biz", result.get("mail").asText());
		Assertions.assertEquals("Gwenborough, Kulas Light", result.get("address").asText());

		Assertions.assertEquals("simpleUser", result.get("docType").asText());

	}

	@Test
	public void schemaParsing16() throws IOException, URISyntaxException {
		String schema = Files.readString(Path.of(resourceLoader.getResource("schema/30.json").getURI()));
		String source = Files.readString(Path.of(resourceLoader.getResource("source/16.json").getURI()));
		JsonNode result = schemaEngine.process(schema, source);
		Assertions.assertEquals("Leanne Graham", result.get("id").asText());
		Assertions.assertEquals("1", result.get("more-id").asText());
		Assertions.assertEquals("Mario", result.get("utenti").get(0).get("name").asText());

	}

	@SpringBootApplication
	static class TestConfiguration {
	}

}
