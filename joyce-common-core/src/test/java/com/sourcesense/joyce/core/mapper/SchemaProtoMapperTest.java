package com.sourcesense.joyce.core.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.sourcesense.joyce.core.mapping.mapper.ProtoConverter;
import com.sourcesense.joyce.core.mapping.mapper.SchemaProtoMapper;
import com.sourcesense.joyce.core.mapping.mapper.SchemaProtoMapperImpl;
import com.sourcesense.joyce.core.model.entity.SchemaEntity;
import com.sourcesense.joyce.core.model.uri.JoyceSchemaURI;
import com.sourcesense.joyce.core.model.uri.JoyceURIFactory;
import com.sourcesense.joyce.core.test.TestUtility;
import com.sourcesense.joyce.protobuf.model.Schema;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@ExtendWith(MockitoExtension.class)
public class SchemaProtoMapperTest implements TestUtility {

	private static final String SCHEMA_ENTITY_PATH = "mapper/schemaProtoMapper/schema/entity/01.json";
	private static final String SCHEMA_PROTO_PATH = "mapper/schemaProtoMapper/schema/proto/01.json";

	private static final String JOYCE_URI_STRING = "joyce:content:test:default:user:schema";
	private static final JoyceSchemaURI JOYCE_URI_OBJECT = JoyceURIFactory.getInstance().createURIOrElseThrow(JOYCE_URI_STRING, JoyceSchemaURI.class);

	private ProtoConverter protoConverter;
	private SchemaProtoMapper schemaMapper;

	@BeforeEach
	public void init() {
		protoConverter = new ProtoConverter();
		schemaMapper = new SchemaProtoMapperImpl();
		schemaMapper.setJsonMapper(this.jsonMapper);
		schemaMapper.setProtoConverter(protoConverter);
	}

	@Test
	public void shouldConvertSchemaProtoToEntity() throws IOException, URISyntaxException {
		String proto = this.computeResourceAsString(SCHEMA_PROTO_PATH);
		String entity = this.computeResourceAsString(SCHEMA_ENTITY_PATH);

		Schema schemaProto = protoConverter.jsonToProtoOrElseThrow(proto, Schema.class);
		SchemaEntity schemaEntity = jsonMapper.readValue(entity, SchemaEntity.class);

		assertEquals(schemaEntity, schemaMapper.protoToEntity(schemaProto));
	}

	@Test
	public void shouldConvertSchemaProtosToEntities() throws IOException, URISyntaxException {
		String proto = this.computeResourceAsString(SCHEMA_PROTO_PATH);
		String entity = this.computeResourceAsString(SCHEMA_ENTITY_PATH);

		Schema schemaProto = protoConverter.jsonToProtoOrElseThrow(proto, Schema.class);
		SchemaEntity schemaEntity = jsonMapper.readValue(entity, SchemaEntity.class);

		assertEquals(
				Collections.singletonList(schemaEntity),
				schemaMapper.protosToEntities(Collections.singletonList(schemaProto))
		);
	}

	@Test
	public void shouldConvertSchemaEntityToProto() throws IOException, URISyntaxException {
		String proto = this.computeResourceAsString(SCHEMA_PROTO_PATH);
		String entity = this.computeResourceAsString(SCHEMA_ENTITY_PATH);

		Schema schemaProto = protoConverter.jsonToProtoOrElseThrow(proto, Schema.class);
		SchemaEntity schemaEntity = jsonMapper.readValue(entity, SchemaEntity.class);

		assertEquals(schemaProto,	schemaMapper.entityToProto(schemaEntity));
	}

	@Test
	public void shouldConvertSchemaEntitiesToProtos() throws IOException, URISyntaxException {
		String proto = this.computeResourceAsString(SCHEMA_PROTO_PATH);
		String entity = this.computeResourceAsString(SCHEMA_ENTITY_PATH);

		Schema schemaProto = protoConverter.jsonToProtoOrElseThrow(proto, Schema.class);
		SchemaEntity schemaEntity = jsonMapper.readValue(entity, SchemaEntity.class);

		assertEquals(
				Collections.singletonList(schemaEntity),
				schemaMapper.protosToEntities(Collections.singletonList(schemaProto))
		);
	}

	@Test
	public void shouldConvertJoyceUriToString() {
		assertEquals(
				JOYCE_URI_STRING,
				schemaMapper.joyceSchemaURIToString(JOYCE_URI_OBJECT)
		);
	}

	@Test
	public void shouldConvertStringToJoyceUri() {
		assertEquals(
				JOYCE_URI_OBJECT,
				schemaMapper.stringToJoyceSchemaURI(JOYCE_URI_STRING)
		);
	}

	@Test
	public void shouldConvertStringToMap() throws IOException, URISyntaxException {
		String json = this.computeResourceAsString(SCHEMA_ENTITY_PATH);
		Map<String, Object> map = this.computeResourceAsObject(SCHEMA_ENTITY_PATH, new TypeReference<>() {});

		assertEquals(map,	schemaMapper.stringToMap(json));
	}

	@Test
	public void shouldConvertMapToString() throws IOException, URISyntaxException {
		JsonNode json = this.computeResourceAsObject(SCHEMA_ENTITY_PATH, JsonNode.class);
		Map<String, Object> map = this.computeResourceAsObject(SCHEMA_ENTITY_PATH, new TypeReference<>() {});

		assertEquals(json.toString(), schemaMapper.mapToString(map));
	}

	@Test
	public void shouldConvertStructToJsonNode() throws IOException, URISyntaxException {
		String json = this.computeResourceAsString(SCHEMA_ENTITY_PATH);

		assertEquals(
			jsonMapper.readTree(json),
			schemaMapper.structToJsonNode(this.computeStruct(json))
		);
	}

	@Test
	public void shouldConvertJsonNodeToStruct() throws IOException, URISyntaxException {
		String json = this.computeResourceAsString(SCHEMA_ENTITY_PATH);

		assertEquals(
				this.computeStruct(json),
				schemaMapper.jsonNodeToStruct(jsonMapper.readTree(json))
		);
	}

	@Test
	public void shouldConvertNullToNull() throws JsonProcessingException {
		assertNull(schemaMapper.protoToEntity(null));
		assertNull(schemaMapper.entityToProto(null));
		assertNull(schemaMapper.protosToEntities((List<Schema>) null));
		assertNull(schemaMapper.entitiesToProtos(null));
		assertNull(schemaMapper.joyceSchemaURIToString(null));
		assertNull(schemaMapper.stringToJoyceSchemaURI(null));
		assertNull(schemaMapper.stringToMap(null));
		assertNull(schemaMapper.mapToString(null));
		assertNull(schemaMapper.structToJsonNode(null));
		assertNull(schemaMapper.jsonNodeToStruct(null));
	}
}
