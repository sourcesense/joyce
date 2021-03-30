package com.sourcesense.nile.ingestion.core.service;

import com.sourcesense.nile.ingestion.core.dao.Dao;
import com.sourcesense.nile.ingestion.core.dto.Schema;
import com.sourcesense.nile.ingestion.core.dto.SchemaShort;
import com.sourcesense.nile.ingestion.core.mapper.SchemaMapper;
import com.sourcesense.nile.ingestion.core.model.SchemaEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SchemaService {
	private final Dao<SchemaEntity> schemaEntityDao;
	private final SchemaMapper schemaMapper;

	public Schema save(Schema schema) {
		SchemaEntity entity = schemaMapper.toEntity(schema);
		schemaEntityDao.save(entity);
		// TODO get with a find
		return schema;
	}

	public List<SchemaShort> findAll() {
		return schemaEntityDao.getAll().stream()
				.map(schemaMapper::toDtoShort)
				.collect(Collectors.toList());
	}
}
