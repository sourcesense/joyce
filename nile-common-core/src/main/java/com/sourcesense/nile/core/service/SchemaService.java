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
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@ConditionalOnProperty(value = "nile.schema-service.enabled", havingValue = "true")
@Service
@RequiredArgsConstructor
public class SchemaService {
	private final SchemaDao schemaEntityDao;
	private final SchemaMapper schemaMapper;
	private final SchemaEngine schemaEngine;

	@Value("${nile.schema-service.uidPattern:nile://ingestion/schema/%s}")
	String uidPattern;


	public Schema save(SchemaSave schema) throws JsonProcessingException {
		SchemaEntity entity = schemaMapper.toEntity(schema);
		String uid = String.format(uidPattern, entity.getName());
		entity.setUid(uid);
		// TODO: validate schema with schemaEngine
		Optional<SchemaEntity> previous = schemaEntityDao.get(uid);

		if (previous.isPresent()){
			if(entity.getDevelopment()){
				// If schema is in development mode we skip schema checks, but we make shure to not break previous version by stepping up the version
				if (!previous.get().getDevelopment()){
					previous.get().setUid(String.format("%s/%d", uid, previous.get().getVersion()));
					schemaEntityDao.save(previous.get());
					entity.setVersion(previous.get().getVersion()+1);
				}
			} else {
				Boolean breakingChanges = schemaEngine.hasBreakingChanges(previous.get().getSchema(), entity.getSchema());
				if (breakingChanges){
					previous.get().setUid(String.format("%s/%d", uid, previous.get().getVersion()));
					schemaEntityDao.save(previous.get());
					entity.setVersion(previous.get().getVersion()+1);
				} else {
					entity.setVersion(previous.get().getVersion());
				}
			}

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

	public Optional<Schema> findByNameAndVersion(String name, Integer version) {
		//TODO: make a specific method on dao to optimize ???
		List<SchemaEntity> versions = schemaEntityDao.getByName(name);
		return versions.stream()
				.filter(schemaEntity -> schemaEntity.getVersion() == version)
				.map(schemaMapper::toDto)
				.findFirst();
	}

	public void delete(String name) {
		Optional<SchemaEntity> entity = schemaEntityDao.get(String.format(uidPattern, name));
		if(entity.isEmpty()){
			throw new SchemaNotFoundException(String.format("Schema [%s] does not exists", name));
		}
		schemaEntityDao.delete(entity.get());
	}

	public List<SchemaShort> getAllVersions(String name) {
		List<SchemaEntity> versions = schemaEntityDao.getByName(name);
		return versions.stream()
				.map(schemaMapper::toDtoShort)
				.collect(Collectors.toList());
	}


}
