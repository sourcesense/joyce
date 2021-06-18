package com.sourcesense.joyce.sink.mongodb.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sourcesense.joyce.sink.mongodb.model.SchemaObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.swing.text.Document;

class CollectionEnhancerTest {

	private ObjectMapper mapper;
	private CollectionEnhancer collectionEnhancer;

	@BeforeEach
	void init() {
		this.mapper = new ObjectMapper();
//		this.collectionEnhancer = new CollectionEnhancer(null, null, null);
	}

	@Test
	void thisWillNeverWork() throws JsonProcessingException {
		String schema = "{\n" +
				"  \"type\": \"object\",\n" +
				"  \"required\": [],\n" +
				"  \"properties\": {\n" +
				"    \"stringProp\": {\n" +
				"      \"type\": \"string\"\n" +
				"    },\n" +
				"    \"booleanProp\": {\n" +
				"      \"type\": \"boolean\"\n" +
				"    },\n" +
				"    \"integerProp\": {\n" +
				"      \"type\": \"integer\"\n" +
				"    },\n" +
				"    \"numberProp\": {\n" +
				"      \"type\": \"number\"\n" +
				"    },\n" +
				"    \"objectProp\": {\n" +
				"      \"type\": \"object\",\n" +
				"      \"required\": [],\n" +
				"      \"properties\": {\n" +
				"        \"nestedProp\": {\n" +
				"          \"type\": \"string\",\n" +
				"          \"required\": []\n" +
				"        }\n" +
				"      }\n" +
				"    },\n" +
				"    \"arrayProp\": {\n" +
				"      \"type\": \"array\",\n" +
				"      \"items\": {\n" +
				"        \"type\": \"object\",\n" +
				"        \"properties\": {\n" +
				"          \"booleanProp\": {\n" +
				"            \"type\": \"boolean\"\n" +
				"          },\n" +
				"          \"integerProp\": {\n" +
				"            \"type\": \"integer\"\n" +
				"          },\n" +
				"          \"numberProp\": {\n" +
				"            \"type\": \"number\"\n" +
				"          },\n" +
				"          \"objectProp\": {\n" +
				"            \"type\": \"object\",\n" +
				"            \"required\": [],\n" +
				"            \"properties\": {\n" +
				"              \"nestedProp\": {\n" +
				"                \"type\": \"string\",\n" +
				"                \"required\": []\n" +
				"              }\n" +
				"            }\n" +
				"          }\n" +
				"        }\n" +
				"      }\n" +
				"    }\n" +
				"  }\n" +
				"}";

		JsonNode jsonNode = mapper.readTree(schema);
		SchemaObject object = mapper.convertValue(jsonNode, SchemaObject.class);
		Document document = mapper.convertValue(object, Document.class);
	}
}
