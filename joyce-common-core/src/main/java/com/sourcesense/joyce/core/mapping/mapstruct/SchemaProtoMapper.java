package com.sourcesense.joyce.core.mapping.mapstruct;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
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

import java.util.Iterator;
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
	@Mapping(target = "metadata.parent", source = "metadata.parent", qualifiedByName = "stringToJoyceSchemaURI")
	@Mapping(target = "metadata.extra", source = "metadata.extra", qualifiedByName = "stringToMap")
	@Mapping(target = "properties", source = "properties", qualifiedByName = "structToJsonNode")
	public abstract SchemaEntity protoToEntity(Schema schema);

	public abstract List<SchemaEntity> protosToEntities(List<Schema> schemas);

	public List<SchemaEntity> protosToEntities(Iterator<Schema> schemas) {
		return this.protosToEntities(
				Lists.newArrayList(schemas)
		);
	}

	@Mapping(target = "uid", source = "uid", qualifiedByName = "joyceSchemaURIToString")
	@Mapping(target = "metadata.parent", source = "metadata.parent", qualifiedByName = "joyceSchemaURIToString")
	@Mapping(target = "metadata.extra", source = "metadata.extra", qualifiedByName = "mapToString")
	@Mapping(target = "properties", source = "properties", qualifiedByName = "jsonNodeToStruct")
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
