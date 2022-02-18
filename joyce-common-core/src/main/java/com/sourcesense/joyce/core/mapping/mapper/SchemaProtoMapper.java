package com.sourcesense.joyce.core.mapping.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.protobuf.Struct;
import com.sourcesense.joyce.core.model.JoyceURI;
import com.sourcesense.joyce.core.model.SchemaEntity;
import com.sourcesense.joyce.protobuf.enumeration.JoyceUriSubtype;
import com.sourcesense.joyce.protobuf.model.Schema;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.NullValueCheckStrategy;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.function.Predicate;

@Mapper(nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public abstract class SchemaProtoMapper {

	protected ObjectMapper jsonMapper;
	protected ProtoConverter protoConverter;

	@Autowired
	public void setJsonMapper(ObjectMapper jsonMapper) {
		this.jsonMapper = jsonMapper;
	}

	@Autowired
	public void setProtoConverter(ProtoConverter protoConverter) {
		this.protoConverter = protoConverter;
	}

	@Mapping(target = "properties", source = "properties", qualifiedByName = "structToJsonNode")
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

	@Mapping(target = "properties", source = "properties", qualifiedByName = "jsonNodeToStruct")
	@Mapping(target = "metadata.subtype", source = "metadata.subtype", qualifiedByName = "joyceUriSubtypeEntityToProto")
	@Mapping(target = "metadata.parent", source = "metadata.parent", qualifiedByName = "joyceUriEntityToString")
	@Mapping(target = "metadata.extra", source = "metadata.extra", qualifiedByName = "mapToString")
	public abstract Schema entityToProto(SchemaEntity schema);

	public abstract List<Schema> entitiesToProtos(List<SchemaEntity> schema);

	@Named("joyceUriEntityToString")
	public String joyceUriEntityToString(JoyceURI joyceURI) {
		return ObjectUtils.isNotEmpty(joyceURI) ? joyceURI.toString() : null;
	}

	@Named("joyceUriStringToEntity")
	public JoyceURI joyceUriStringToEntity(String joyceUri) {
		return JoyceURI.createURI(joyceUri).orElse(null);
	}

	@Named("joyceUriSubtypeEntityToProto")
	public JoyceUriSubtype joyceUriSubtypeEntityToProto(JoyceURI.Subtype subtype) {
		return Optional.ofNullable(subtype)
				.map(JoyceURI.Subtype::name)
				.map(JoyceUriSubtype::valueOf)
				.orElse(null);
	}

	@Named("joyceUriSubtypeProtoToEntity")
	public JoyceURI.Subtype joyceUriSubtypeProtoToEntity(JoyceUriSubtype subtype) {
		return Optional.ofNullable(subtype)
				.filter(Predicate.not(JoyceUriSubtype.UNRECOGNIZED::equals))
				.map(JoyceUriSubtype::name)
				.map(JoyceURI.Subtype::valueOf)
				.orElse(null);
	}

	@Named("stringToMap")
	public Map<String, Object> stringToMap(String string) throws JsonProcessingException {
		TypeReference<Map<String, Object>> mapType = new TypeReference<>() {};
		return ObjectUtils.isNotEmpty(string) ? jsonMapper.readValue(string, mapType) : null;
	}

	@Named("mapToString")
	public String mapToString(Map<String, Object> map) throws JsonProcessingException {
		return ObjectUtils.isNotEmpty(map) ? jsonMapper.writeValueAsString(map) : null;
	}

	@Named("structToJsonNode")
	public JsonNode structToJsonNode(Struct struct) throws JsonProcessingException {
		return Optional.ofNullable(struct)
				.map(protoConverter::protoToJsonOrElseThrow)
				.flatMap(this::readTree)
				.orElse(null);
	}

	@Named("jsonNodeToStruct")
	public Struct jsonNodeToStruct(JsonNode jsonNode) {
		return Optional.ofNullable(jsonNode)
				.map(JsonNode::toPrettyString)
				.flatMap(json -> protoConverter.jsonToProto(json, Struct.class))
				.orElse(null);
	}

	private Optional<JsonNode> readTree(String json) {
		try {
			return Optional.ofNullable(
					jsonMapper.readTree(json)
			);
		} catch (Exception exception) {
			return Optional.empty();
		}
	}
}
