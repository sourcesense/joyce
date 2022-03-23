package com.sourcesense.joyce.importcore.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.sourcesense.joyce.core.model.entity.JoyceSchemaMetadata;
import com.sourcesense.joyce.importcore.test.TestUtility;
import io.github.jamsesso.jsonlogic.JsonLogic;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.io.IOException;
import java.net.URISyntaxException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class JsonLogicServiceTest implements TestUtility {

	private JsonLogicService jsonLogicService;

	@BeforeEach
	public void init() {
		JsonLogic jsonLogic = new JsonLogic();
		jsonLogicService = new JsonLogicService(jsonMapper, jsonLogic);
	}

	@Test
	void shouldAllowMatchingDocument() throws IOException, URISyntaxException {

		JsonNode document = this.computeResourceAsObject("document/01.json", JsonNode.class);
		JoyceSchemaMetadata metadata = this.computeResourceAsObject("metadata/01.json", JoyceSchemaMetadata.class);

		assertTrue(jsonLogicService.filter(document, metadata));
	}

	@Test
	void shouldAllowMissingFilter() throws IOException, URISyntaxException {

		JsonNode document = this.computeResourceAsObject("document/01.json", JsonNode.class);
		JoyceSchemaMetadata metadata = new JoyceSchemaMetadata();

		assertTrue(jsonLogicService.filter(document, metadata));
	}

	@Test
	void shouldFilterNotMatchingDocument() throws IOException, URISyntaxException {

		JsonNode document = this.computeResourceAsObject("document/02.json", JsonNode.class);
		JoyceSchemaMetadata metadata = this.computeResourceAsObject("metadata/02.json", JoyceSchemaMetadata.class);

		assertFalse(jsonLogicService.filter(document, metadata));
	}
}
