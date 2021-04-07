package com.sourcesense.nile.core.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sourcesense.nile.core.dto.Schema;
import com.sourcesense.nile.core.dto.SchemaSave;
import com.sourcesense.nile.core.dto.SchemaShort;
import com.sourcesense.nile.core.model.SchemaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;


@Mapper
@Component
public abstract class SchemaMapper {
	@Autowired
	ObjectMapper mapper;

	@Mapping(target = "schema", source = "entity")
	public abstract Schema toDto(SchemaEntity entity);

	public abstract SchemaShort toDtoShort(SchemaEntity entity);

	@Mapping(target = "schema", source = "dto")
	public abstract SchemaEntity toEntity(SchemaSave dto);

	String schemaAsString(SchemaSave dto) throws JsonProcessingException {
		return mapper.writeValueAsString(dto.getSchema());
	}

	Map schemaAsMap(SchemaEntity entity) throws JsonProcessingException {
		return mapper.readValue(entity.getSchema(), Map.class);
	}

}
