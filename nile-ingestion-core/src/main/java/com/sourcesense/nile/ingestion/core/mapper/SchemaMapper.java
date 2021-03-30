package com.sourcesense.nile.ingestion.core.mapper;

import com.sourcesense.nile.ingestion.core.dto.Schema;
import com.sourcesense.nile.ingestion.core.dto.SchemaShort;
import com.sourcesense.nile.ingestion.core.model.SchemaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.stereotype.Component;


@Mapper
@Component
public interface SchemaMapper {
	Schema toDto(SchemaEntity entity);
	SchemaShort toDtoShort(SchemaEntity entity);

	@Mapping(source = "uid", target = "uid")
	SchemaEntity toEntity(Schema dto);
}
