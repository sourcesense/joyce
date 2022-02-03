package com.sourcesense.joyce.core.mapping.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.sourcesense.joyce.core.model.JoyceURI;
import com.sourcesense.joyce.core.model.SchemaEntity;
import com.sourcesense.joyce.protobuf.enumeration.JoyceUriSubtype;
import com.sourcesense.joyce.protobuf.model.Schema;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

@Mapper
public abstract class SchemaProtoMapper {

	protected ObjectMapper jsonMapper;

	@Autowired
	private void setJsonMapper(ObjectMapper jsonMapper) {
		this.jsonMapper = jsonMapper;
	}

	@Mapping(target = "properties", source = "properties", qualifiedByName = "stringToJsonNode")
	@Mapping(target = "metadata.subtype", source = "metadata.subtype", qualifiedByName = "joyceUriSubtypeProtoToEntity")
	@Mapping(target = "metadata.parent", source = "metadata.parent", qualifiedByName = "joyceUriStringToEntity")
	@Mapping(target = "metadata.extra", source = "metadata.extra", qualifiedByName = "stringToMap")
	public abstract SchemaEntity protoToEntity(Schema schema);

	public abstract List<SchemaEntity> protosToEntities(List<Schema> schemas);

	public List<SchemaEntity> protosToEntities(Iterator<Schema> schemas) {
		return this.protosToEntities(
				Lists.newArrayList(schemas)
		);
	}

	@Mapping(target = "properties", source = "properties", qualifiedByName = "jsonNodeToString")
	@Mapping(target = "metadata.subtype", source = "metadata.subtype", qualifiedByName = "joyceUriSubtypeEntityToProto")
	@Mapping(target = "metadata.parent", source = "metadata.parent", qualifiedByName = "joyceUriEntityToString")
	@Mapping(target = "metadata.extra", source = "metadata.extra", qualifiedByName = "mapToString")
	public abstract Schema entityToProto(SchemaEntity schema);

	public abstract List<Schema> entitiesToProtos(List<SchemaEntity> schema);

	@Named("joyceUriEntityToString")
	public String joyceUriEntityToString(JoyceURI joyceURI) {
		return Objects.nonNull(joyceURI) ? joyceURI.toString() : StringUtils.EMPTY;
	}

	@Named("joyceUriStringToEntity")
	public JoyceURI joyceUriStringToEntity(String joyceUri) {
		return JoyceURI.createURI(joyceUri).orElse(null);
	}

	@Named("joyceUriSubtypeEntityToProto")
	public JoyceUriSubtype joyceUriSubtypeEntityToProto(JoyceURI.Subtype subtype) {
		return JoyceUriSubtype.valueOf(subtype.name());
	}

	@Named("joyceUriSubtypeProtoToEntity")
	public JoyceURI.Subtype joyceUriSubtypeProtoToEntity(JoyceUriSubtype subtype) {
		return JoyceURI.Subtype.valueOf(subtype.name());
	}

	@Named("stringToMap")
	public Map<String, Object> stringToMap(String string) throws JsonProcessingException {
		TypeReference<Map<String, Object>> mapType = new TypeReference<>() {};
		return Objects.nonNull(string) ? jsonMapper.readValue(string, mapType) : new HashMap<>();
	}

	@Named("mapToString")
	public String mapToString(Map<String, Object> map) throws JsonProcessingException {
		return Objects.nonNull(map) ? jsonMapper.writeValueAsString(map) : StringUtils.EMPTY;
	}

	@Named("stringToJsonNode")
	public JsonNode stringToJsonNode(String string) throws JsonProcessingException {
		return Objects.nonNull(string) ? jsonMapper.readTree(string) : null;
	}

	@Named("jsonNodeToString")
	public String jsonNodeToString(JsonNode jsonNode) throws JsonProcessingException {
		return Objects.nonNull(jsonNode) ? jsonMapper.writeValueAsString(jsonNode) : StringUtils.EMPTY;
	}
}
