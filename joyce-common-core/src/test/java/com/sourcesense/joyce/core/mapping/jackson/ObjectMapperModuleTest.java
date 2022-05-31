package com.sourcesense.joyce.core.mapping.jackson;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.sourcesense.joyce.core.model.uri.JoyceURI;
import com.sourcesense.joyce.core.model.uri.JoyceURIWrapper;
import com.sourcesense.joyce.core.test.CommonCoreJoyceTest;
import org.junit.jupiter.api.Test;

import static com.sourcesense.joyce.core.model.uri.JoyceURIValues.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ObjectMapperModuleTest extends CommonCoreJoyceTest {

	@Test
	public void shouldConvertJoyceTaxonomyURIToString() throws JsonProcessingException {
		this.testJoyceURIToStringConversion(JOYCE_TAXONOMY_URI, TAXONOMY_URI);
	}

	@Test
	public void shouldConvertJoyceSchemaURIToString() throws JsonProcessingException {
		this.testJoyceURIToStringConversion(JOYCE_SCHEMA_URI, SCHEMA_URI);
	}

	@Test
	public void shouldConvertJoyceSourceURIToString() throws JsonProcessingException {
		this.testJoyceURIToStringConversion(JOYCE_SOURCE_URI, SOURCE_URI);
	}

	@Test
	public void shouldConvertJoyceDocumentURIToString() throws JsonProcessingException {
		this.testJoyceURIToStringConversion(JOYCE_DOCUMENT_URI, DOCUMENT_URI);
	}

	@Test
	public void shouldConvertStringToTaxonomyURI() throws JsonProcessingException {
		this.testStringToJoyceURIConversion(TAXONOMY_URI, JOYCE_TAXONOMY_URI);
	}

	@Test
	public void shouldConvertStringToSchemaURI() throws JsonProcessingException {
		this.testStringToJoyceURIConversion(SCHEMA_URI, JOYCE_SCHEMA_URI);
	}

	@Test
	public void shouldConvertStringToSourceURI() throws JsonProcessingException {
		this.testStringToJoyceURIConversion(SOURCE_URI, JOYCE_SOURCE_URI);
	}

	@Test
	public void shouldConvertStringToDocumentURI() throws JsonProcessingException {
		this.testStringToJoyceURIConversion(DOCUMENT_URI, JOYCE_DOCUMENT_URI);
	}

	private <J extends JoyceURI> void testJoyceURIToStringConversion(J joyceURI, String stringURI) throws JsonProcessingException {
		assertEquals(
				this.computeStringURIWrapper(stringURI),
				jsonMapper.writeValueAsString(new JoyceURIWrapper<>(666, joyceURI))
		);
	}

	private <J extends JoyceURI> void testStringToJoyceURIConversion(String stringURI, J joyceURI) throws JsonProcessingException {
		JoyceURIWrapper<J> expected = new JoyceURIWrapper<>(666, joyceURI);
		JoyceURIWrapper<J> actual = jsonMapper.readValue(
				this.computeStringURIWrapper(stringURI),
				new TypeReference<>() {}
		);

		assertEquals(expected, actual);
	}

	private String computeStringURIWrapper(String stringURI) {
		return jsonMapper.createObjectNode()
				.put("_id", 666)
				.put("joyceURI", stringURI)
				.toString();
	}
}
