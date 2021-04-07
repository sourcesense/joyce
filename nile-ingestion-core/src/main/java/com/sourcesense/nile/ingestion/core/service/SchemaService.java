package com.sourcesense.nile.ingestion.core.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sourcesense.nile.ingestion.core.dao.Dao;
import com.sourcesense.nile.ingestion.core.dto.Schema;
import com.sourcesense.nile.ingestion.core.dto.SchemaSave;
import com.sourcesense.nile.ingestion.core.dto.SchemaShort;
import com.sourcesense.nile.ingestion.core.errors.SchemaNotFoundException;
import com.sourcesense.nile.ingestion.core.mapper.SchemaMapper;
import com.sourcesense.nile.ingestion.core.model.SchemaEntity;
import com.sourcesense.nile.schemaengine.service.SchemaEngine;
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
	private final SchemaEngine schemaEngine;

	public static String UID_PATTERN = "nile://ingestion/schema/";

	public Schema save(SchemaSave schema) throws JsonProcessingException {
		SchemaEntity entity = schemaMapper.toEntity(schema);
		String uid = UID_PATTERN+entity.getName();
		entity.setUid(uid);

		Optional<SchemaEntity> previous = schemaEntityDao.get(uid);

		if (previous.isPresent()){
			Boolean breakingChanges = schemaEngine.hasBreakingChanges(previous.get().getSchema(), entity.getSchema());
			if (breakingChanges){
				// step version and store previous with changed id
				previous.get().setUid(String.format("%s/%d", uid, previous.get().getVersion()));
				entity.setVersion(previous.get().getVersion()+1);
			} else {
				entity.setVersion(previous.get().getVersion());
			}
			schemaEntityDao.save(previous.get());
		} else {
			// First version of the schema
			entity.setVersion(1);
		}

		schemaEntityDao.save(entity);
		Optional<SchemaEntity> saved = schemaEntityDao.get(uid); // TODO ??? move to DAO this logic??
		return saved.map(schemaMapper::toDto).get();
	}

	public List<SchemaShort> findAll() {
		return schemaEntityDao.getAll().stream()
				.map(schemaMapper::toDtoShort)
				.collect(Collectors.toList());
	}

    public Optional<Schema> findById(String id) {
			if (!id.startsWith(UID_PATTERN)){
				id = UID_PATTERN+id;
			}
			return schemaEntityDao.get(id).map(schemaMapper::toDto);
    }

	public void delete(String id) {
		if (!id.startsWith(UID_PATTERN)){
			id = UID_PATTERN+id;
		}
		Optional<SchemaEntity> entity = schemaEntityDao.get(id);
		if(entity.isEmpty()){
			throw new SchemaNotFoundException(String.format("Schema [%s] does not exists", id));
		}
		schemaEntityDao.delete(entity.get());
	}
}
