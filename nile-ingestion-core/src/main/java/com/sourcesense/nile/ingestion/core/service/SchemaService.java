package com.sourcesense.nile.ingestion.core.service;

import com.sourcesense.nile.ingestion.core.dao.Dao;
import com.sourcesense.nile.ingestion.core.dto.Schema;
import com.sourcesense.nile.ingestion.core.dto.SchemaSave;
import com.sourcesense.nile.ingestion.core.dto.SchemaShort;
import com.sourcesense.nile.ingestion.core.errors.SchemaNotFoundException;
import com.sourcesense.nile.ingestion.core.mapper.SchemaMapper;
import com.sourcesense.nile.ingestion.core.model.SchemaEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SchemaService {
	private final Dao<SchemaEntity> schemaEntityDao;
	private final SchemaMapper schemaMapper;

	public Schema save(SchemaSave schema) {
		SchemaEntity entity = schemaMapper.toEntity(schema);
		schemaEntityDao.save(entity);
		Optional<SchemaEntity> saved = schemaEntityDao.get(entity.getUid());
		// TODO ??? move to DAO this logic??
		return saved.map(schemaMapper::toDto).get();
	}

	public List<SchemaShort> findAll() {
		return schemaEntityDao.getAll().stream()
				.map(schemaMapper::toDtoShort)
				.collect(Collectors.toList());
	}

    public Optional<Schema> findById(String id) {
			return schemaEntityDao.get(id).map(schemaMapper::toDto);
    }

	public void delete(String id) {
		Optional<SchemaEntity> entity = schemaEntityDao.get(id);
		if(entity.isEmpty()){
			throw new SchemaNotFoundException(String.format("Schema [%s] does not exists", id));
		}
		schemaEntityDao.delete(entity.get());
	}
}
