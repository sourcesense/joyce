package com.sourcesense.joyce.core.mapping.mapstruct;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.Struct;
import com.sourcesense.joyce.core.model.entity.SchemaEntity;
import com.sourcesense.joyce.core.model.uri.JoyceSchemaURI;
import com.sourcesense.joyce.core.model.uri.JoyceURIFactory;
import com.sourcesense.joyce.protobuf.model.Schema;
import org.apache.commons.lang3.ObjectUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.NullValueCheckStrategy;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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

	@Mapping(target = "uid", source = "uid", qualifiedByName = "stringToJoyceSchemaURI")
	@Mapping(target = "apply", source = "apply", qualifiedByName = "structToMap")
	@Mapping(target = "properties", source = "properties", qualifiedByName = "structToMap")
	@Mapping(target = "metadata.parent", source = "metadata.parent", qualifiedByName = "stringToJoyceSchemaURI")
	@Mapping(target = "metadata.data", source = "metadata.data", qualifiedByName = "structToMap")
	@Mapping(target = "metadata.extra", source = "metadata.extra", qualifiedByName = "structToMap")
	public abstract SchemaEntity protoToEntity(Schema schema);

	public abstract List<SchemaEntity> protosToEntities(List<Schema> schemas);

	@Mapping(target = "uid", source = "uid", qualifiedByName = "joyceSchemaURIToString")
	@Mapping(target = "apply", source = "apply", qualifiedByName = "mapToStruct")
	@Mapping(target = "properties", source = "properties", qualifiedByName = "mapToStruct")
	@Mapping(target = "metadata.parent", source = "metadata.parent", qualifiedByName = "joyceSchemaURIToString")
	@Mapping(target = "metadata.data", source = "metadata.data", qualifiedByName = "mapToStruct")
	@Mapping(target = "metadata.extra", source = "metadata.extra", qualifiedByName = "mapToStruct")
	public abstract Schema entityToProto(SchemaEntity schema);

	public abstract List<Schema> entitiesToProtos(List<SchemaEntity> schema);

	@Named("joyceSchemaURIToString")
	public String joyceSchemaURIToString(JoyceSchemaURI schemaURI) {
		return ObjectUtils.isNotEmpty(schemaURI) ? schemaURI.toString() : null;
	}

	@Named("stringToJoyceSchemaURI")
	public JoyceSchemaURI stringToJoyceSchemaURI(String stringURI) {
		return JoyceURIFactory.getInstance().createURI(stringURI, JoyceSchemaURI.class).orElse(null);
	}

	@Named("structToMap")
	public Map<String, Object> structToMap(Struct struct) {
		return Optional.ofNullable(struct)
				.map(protoConverter::protoToJsonOrElseThrow)
				.flatMap(this::stringToMap)
				.orElse(null);
	}

	@Named("mapToStruct")
	public Struct mapToStruct(Map<String, Object> map) {
		return Optional.ofNullable(map)
				.flatMap(this::mapToString)
				.map(json -> protoConverter.jsonToProtoOrElseThrow(json, Struct.class))
				.orElse(null);
	}

	private Optional<Map<String, Object>> stringToMap(String json) {
		try {
			return Optional.ofNullable(
					jsonMapper.readValue(json, new TypeReference<>() {})
			);
		} catch (Exception exception) {
			return Optional.empty();
		}
	}

	private Optional<String> mapToString(Map<String, Object> map) {
		try {
			return Optional.ofNullable(
					jsonMapper.writeValueAsString(map)
			);
		} catch (Exception exception) {
			return Optional.empty();
		}
	}
}
