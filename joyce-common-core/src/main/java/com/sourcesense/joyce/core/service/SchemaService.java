/*
 *  Copyright 2021 Sourcesense Spa
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sourcesense.joyce.core.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sourcesense.joyce.core.configuration.SchemaServiceProperties;
import com.sourcesense.joyce.core.dao.SchemaDao;
import com.sourcesense.joyce.core.dto.Schema;
import com.sourcesense.joyce.core.dto.SchemaSave;
import com.sourcesense.joyce.core.dto.SchemaShort;
import com.sourcesense.joyce.core.exception.SchemaNotFoundException;
import com.sourcesense.joyce.core.mapper.SchemaMapper;
import com.sourcesense.joyce.core.model.JoyceURI;
import com.sourcesense.joyce.core.model.SchemaEntity;
import com.sourcesense.joyce.schemaengine.exception.JoyceSchemaEngineException;
import com.sourcesense.joyce.schemaengine.service.SchemaEngine;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(value = "joyce.schema-service.enabled", havingValue = "true")
public class SchemaService {
	private final SchemaDao schemaEntityDao;
	private final SchemaMapper schemaMapper;
	private final SchemaEngine schemaEngine;
	private final SchemaServiceProperties properties;


	private JoyceURI getSchemaUid(String name){
		return new JoyceURI(URI.create(String.format("joyce://schema/%s/%s", properties.getSubtype(), name)));
	}

	@CacheEvict(cacheNames = "schemas", allEntries=true)
	public JoyceURI save(SchemaSave schema) throws JsonProcessingException {
		SchemaEntity entity = schemaMapper.toEntity(schema);

		JoyceURI uid = getSchemaUid(entity.getMetadata().getName());
		entity.setUid(uid.toString());

		// Validate schema
		schema.getMetadata().validate();

		// If schema has a parent it must exists
		if(schema.getMetadata().getParent() != null){
			Optional<SchemaEntity> parent = schemaEntityDao.get(schema.getMetadata().getParent().toString());
			if (parent.isEmpty()){
				throw new JoyceSchemaEngineException(String.format("Parent schema [%s] does not exists", schema.getMetadata().getParent()));
			}
		}

		Optional<SchemaEntity> previous = schemaEntityDao.get(uid.toString());

		if (previous.isPresent()){
			/**
			 * If schema is in development mode we skip schema checks,
			 * but we block if previous schema is not in dev mode
			 */
			if(entity.getMetadata().getDevelopment() && !previous.get().getMetadata().getDevelopment()){
					throw new JoyceSchemaEngineException("Previous schema is not in development mode");
			}

			if (!entity.getMetadata().getDevelopment()) {
				schemaEngine.checkForBreakingChanges(previous.get().getSchema(), entity.getSchema());
			}
		}

		schemaEntityDao.save(entity);
		return uid;
	}

	@Cacheable("schemas")
	public List<SchemaShort> findAll() {
		return schemaEntityDao.getAll().stream()
				.map(schemaMapper::toDtoShort)
				.collect(Collectors.toList());
	}

	@Cacheable("schemas")
    public Optional<Schema> findByName(String name) {
			return schemaEntityDao.get(getSchemaUid(name).toString()).map(schemaMapper::toDto);
    }

	@CacheEvict(value = "schemas", allEntries=true)
	public void delete(String name) {
		Optional<SchemaEntity> entity = schemaEntityDao.get(getSchemaUid(name).toString());
		if(entity.isEmpty()){
			throw new SchemaNotFoundException(String.format("Schema [%s] does not exists", name));
		}
		schemaEntityDao.delete(entity.get());
	}

	@Cacheable("schemas")
	public Optional<Schema> get(String schemaUid) {
		return schemaEntityDao.get(schemaUid).map(schemaMapper::toDto);
	}
}