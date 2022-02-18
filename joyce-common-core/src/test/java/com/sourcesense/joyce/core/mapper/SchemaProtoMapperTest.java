package com.sourcesense.joyce.core.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sourcesense.joyce.core.mapping.mapper.ProtoConverter;
import com.sourcesense.joyce.core.mapping.mapper.SchemaProtoMapper;
import com.sourcesense.joyce.core.mapping.mapper.SchemaProtoMapperImpl;
import com.sourcesense.joyce.core.model.JoyceURI;
import com.sourcesense.joyce.core.model.SchemaEntity;
import com.sourcesense.joyce.core.test.TestUtility;
import com.sourcesense.joyce.protobuf.enumeration.JoyceUriSubtype;
import com.sourcesense.joyce.protobuf.model.Schema;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@ExtendWith(MockitoExtension.class)
public class SchemaProtoMapperTest implements TestUtility {

	private static final String JOYCE_URI_STRING = "joyce://schema/import/default.test";
	private static final JoyceURI JOYCE_URI_OBJECT = JoyceURI.createURI(JOYCE_URI_STRING).orElseThrow();
	private static final String TEST_SCHEMA_JSON_PATH = "mapper/schemaProtoMapper/schema/01.json";

	private ObjectMapper noAnnotationJsonMapper;
	private ProtoConverter protoConverter;
	private SchemaProtoMapper schemaMapper;

	@BeforeEach
	public void init() {
		noAnnotationJsonMapper = TestUtility.initJsonMapper().configure(MapperFeature.USE_ANNOTATIONS, false);
		protoConverter = new ProtoConverter();
		schemaMapper = new SchemaProtoMapperImpl();
		schemaMapper.setJsonMapper(this.jsonMapper);
		schemaMapper.setProtoConverter(protoConverter);
	}

	@Test
	public void shouldConvertSchemaProtoToEntity() throws IOException, URISyntaxException {
		String json = this.computeResourceAsString(TEST_SCHEMA_JSON_PATH);

		Schema schemaProto = protoConverter.jsonToProtoOrElseThrow(json, Schema.class);
		SchemaEntity schemaEntity = noAnnotationJsonMapper.readValue(json, SchemaEntity.class);

		assertEquals(schemaEntity, schemaMapper.protoToEntity(schemaProto));
	}

	@Test
	public void shouldConvertSchemaProtosToEntities() throws IOException, URISyntaxException {
		String json = this.computeResourceAsString(TEST_SCHEMA_JSON_PATH);

		Schema schemaProto = protoConverter.jsonToProtoOrElseThrow(json, Schema.class);
		SchemaEntity schemaEntity = noAnnotationJsonMapper.readValue(json, SchemaEntity.class);

		assertEquals(
				Collections.singletonList(schemaEntity),
				schemaMapper.protosToEntities(Collections.singletonList(schemaProto))
		);
	}

	@Test
	public void shouldConvertSchemaEntityToProto() throws IOException, URISyntaxException {
		String json = this.computeResourceAsString(TEST_SCHEMA_JSON_PATH);

		Schema schemaProto = protoConverter.jsonToProtoOrElseThrow(json, Schema.class);
		SchemaEntity schemaEntity = noAnnotationJsonMapper.readValue(json, SchemaEntity.class);

		assertEquals(schemaProto,	schemaMapper.entityToProto(schemaEntity));
	}

	@Test
	public void shouldConvertSchemaEntitiesToProtos() throws IOException, URISyntaxException {
		String json = this.computeResourceAsString(TEST_SCHEMA_JSON_PATH);

		Schema schemaProto = protoConverter.jsonToProtoOrElseThrow(json, Schema.class);
		SchemaEntity schemaEntity = noAnnotationJsonMapper.readValue(json, SchemaEntity.class);

		assertEquals(
				Collections.singletonList(schemaEntity),
				schemaMapper.protosToEntities(Collections.singletonList(schemaProto))
		);
	}

	@Test
	public void shouldConvertJoyceUriToString() {
		assertEquals(
				JOYCE_URI_STRING,
				schemaMapper.joyceUriEntityToString(JOYCE_URI_OBJECT)
		);
	}

	@Test
	public void shouldConvertStringToJoyceUri() {
		assertEquals(
				JOYCE_URI_OBJECT,
				schemaMapper.joyceUriStringToEntity(JOYCE_URI_STRING)
		);
	}

	@Test
	public void shouldConvertJoyceUriSubtypeEntityToProto() {
		List<JoyceUriSubtype> expected = Arrays
				.stream(JoyceUriSubtype.values())
				.filter(Predicate.not(JoyceUriSubtype.UNRECOGNIZED::equals))
				.collect(Collectors.toList());

		List<JoyceUriSubtype> actual = Arrays
				.stream(JoyceURI.Subtype.values())
				.map(schemaMapper::joyceUriSubtypeEntityToProto)
				.collect(Collectors.toList());

		assertThat(expected).hasSameElementsAs(actual);
	}

	@Test
	public void shouldConvertJoyceUriSubtypeProtoToEntity() {
		List<JoyceURI.Subtype> expected = Arrays.stream(JoyceURI.Subtype.values())
				.collect(Collectors.toList());

		List<JoyceURI.Subtype> actual = Arrays
				.stream(JoyceUriSubtype.values())
				.filter(Predicate.not(JoyceUriSubtype.UNRECOGNIZED::equals))
				.map(schemaMapper::joyceUriSubtypeProtoToEntity)
				.collect(Collectors.toList());

		assertThat(expected).hasSameElementsAs(actual);
	}

	@Test
	public void shouldConvertStringToMap() throws IOException, URISyntaxException {
		String json = this.computeResourceAsString(TEST_SCHEMA_JSON_PATH);
		Map<String, Object> map = this.computeResourceAsObject(TEST_SCHEMA_JSON_PATH, new TypeReference<>() {});

		assertEquals(map,	schemaMapper.stringToMap(json));
	}

	@Test
	public void shouldConvertMapToString() throws IOException, URISyntaxException {
		JsonNode json = this.computeResourceAsObject(TEST_SCHEMA_JSON_PATH, JsonNode.class);
		Map<String, Object> map = this.computeResourceAsObject(TEST_SCHEMA_JSON_PATH, new TypeReference<>() {});

		assertEquals(json.toString(), schemaMapper.mapToString(map));
	}

	@Test
	public void shouldConvertStructToJsonNode() throws IOException, URISyntaxException {
		String json = this.computeResourceAsString(TEST_SCHEMA_JSON_PATH);

		assertEquals(
			jsonMapper.readTree(json),
			schemaMapper.structToJsonNode(this.computeStruct(json))
		);
	}

	@Test
	public void shouldConvertJsonNodeToStruct() throws IOException, URISyntaxException {
		String json = this.computeResourceAsString(TEST_SCHEMA_JSON_PATH);

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
		assertNull(schemaMapper.joyceUriEntityToString(null));
		assertNull(schemaMapper.joyceUriStringToEntity(null));
		assertNull(schemaMapper.joyceUriSubtypeEntityToProto(null));
		assertNull(schemaMapper.joyceUriSubtypeProtoToEntity(null));
		assertNull(schemaMapper.stringToMap(null));
		assertNull(schemaMapper.mapToString(null));
		assertNull(schemaMapper.structToJsonNode(null));
		assertNull(schemaMapper.jsonNodeToStruct(null));
	}
}
