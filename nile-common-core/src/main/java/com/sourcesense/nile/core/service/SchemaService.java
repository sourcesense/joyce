package com.sourcesense.nile.core.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sourcesense.nile.core.dao.SchemaDao;
import com.sourcesense.nile.core.dto.Schema;
import com.sourcesense.nile.core.dto.SchemaSave;
import com.sourcesense.nile.core.dto.SchemaShort;
import com.sourcesense.nile.core.errors.SchemaNotFoundException;
import com.sourcesense.nile.core.mapper.SchemaMapper;
import com.sourcesense.nile.core.model.SchemaEntity;
import com.sourcesense.nile.schemaengine.service.SchemaEngine;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SchemaService {
	private final SchemaDao schemaEntityDao;
	private final SchemaMapper schemaMapper;
	private final SchemaEngine schemaEngine;

	@Value("${nile.schema.uidPattern:nile://ingestion/schema/%s}")
	String uidPattern;


	public Schema save(SchemaSave schema) throws JsonProcessingException {
		SchemaEntity entity = schemaMapper.toEntity(schema);
		String uid = String.format(uidPattern, entity.getName());
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

    public Optional<Schema> findByName(String name) {
			return schemaEntityDao.get(String.format(uidPattern, name)).map(schemaMapper::toDto);
    }

	public void delete(String name) {
		Optional<SchemaEntity> entity = schemaEntityDao.get(String.format(uidPattern, name));
		if(entity.isEmpty()){
			throw new SchemaNotFoundException(String.format("Schema [%s] does not exists", name));
		}
		schemaEntityDao.delete(entity.get());
	}

	public List<Schema> getAllVersions(String name) {
		List<SchemaEntity> versions = schemaEntityDao.getByName(name);
		return versions.stream()
				.map(schemaMapper::toDto)
				.collect(Collectors.toList());
	}
}
