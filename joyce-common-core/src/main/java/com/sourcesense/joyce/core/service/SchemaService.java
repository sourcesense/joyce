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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sourcesense.joyce.core.dao.SchemaDao;
import com.sourcesense.joyce.core.dto.SchemaSave;
import com.sourcesense.joyce.core.exception.SchemaNotFoundException;
import com.sourcesense.joyce.core.mapper.SchemaMapper;
import com.sourcesense.joyce.core.model.JoyceURI;
import com.sourcesense.joyce.core.model.SchemaEntity;
import com.sourcesense.joyce.schemaengine.exception.JoyceSchemaEngineException;
import com.sourcesense.joyce.schemaengine.service.SchemaEngine;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(value = "joyce.schema-service.enabled", havingValue = "true")
public class SchemaService {

	private final SchemaDao schemaDao;
	private final SchemaMapper schemaMapper;
	private final SchemaEngine schemaEngine;
	private final ObjectMapper objectMapper;


	private JoyceURI getSchemaUid(JoyceURI.Subtype subtype, String namespace, String name) {
		return JoyceURI.makeNamespaced(JoyceURI.Type.SCHEMA, subtype, namespace, name);
	}

	public JoyceURI save(SchemaSave schema) {
		SchemaEntity entity = schemaMapper.toEntity(schema);

		JoyceURI uid = getSchemaUid(
				entity.getMetadata().getSubtype(),
				entity.getMetadata().getNamespace(),
				entity.getMetadata().getName()
		);

		entity.setUid(uid.toString());

		// Validate schema
		schema.getMetadata().validate();

		// If schema has a parent it must exists
		if (schema.getMetadata().getParent() != null) {
			Optional<SchemaEntity> parent = schemaDao.get(schema.getMetadata().getParent().toString());
			if (parent.isEmpty()) {
				throw new JoyceSchemaEngineException(String.format("Parent schema [%s] does not exists", schema.getMetadata().getParent()));
			}
		}

		Optional<SchemaEntity> previous = schemaDao.get(uid.toString());

		if (previous.isPresent()) {
			/*
			 * If schema is in development mode we skip schema checks,
			 * but we block if previous schema is not in dev mode
			 */
			if (entity.getMetadata().getDevelopment() && !previous.get().getMetadata().getDevelopment()) {
				throw new JoyceSchemaEngineException("Previous schema is not in development mode");
			}

			if (!entity.getMetadata().getDevelopment() && !previous.get().getMetadata().getDevelopment()) {
				schemaEngine.checkForBreakingChanges(
						objectMapper.convertValue(previous.get(), JsonNode.class),
						objectMapper.convertValue(entity, JsonNode.class)
				);
			}
		}

		schemaDao.save(entity);
		return uid;
	}

	public List<SchemaEntity> findAll(Boolean rootOnly) {
		return schemaDao.getAll(rootOnly);
	}

	public List<SchemaEntity> findBySubtypeAndNamespace(
			JoyceURI.Subtype subtype,
			String namespace,
			Boolean rootOnly) {

		return schemaDao.getAllBySubtypeAndNamespace(subtype, namespace, rootOnly);
	}

	public List<SchemaEntity> findByReportsNotEmpty() {
		return schemaDao.getAllByReportsNotEmpty();
	}

	public Optional<SchemaEntity> findByName(JoyceURI.Subtype subtype, String namespace, String name) {
		String schemaUri = this.getSchemaUid(subtype, namespace, name).toString();
		return schemaDao.get(schemaUri);
	}

	public SchemaEntity findByNameOrElseThrow(JoyceURI.Subtype subtype, String namespace, String name) {
		return this.findByName(subtype, namespace, name)
				.orElseThrow(
						() -> new SchemaNotFoundException(
								String.format("Schema [%s/%s/%s] does not exists", subtype, namespace, name))
				);
	}

	public Optional<SchemaEntity> findById(String schemaId) {
		return schemaDao.get(schemaId);
	}

	public void delete(JoyceURI.Subtype subtype, String namespace, String name) {
		String schemaUid = this.getSchemaUid(subtype, namespace, name).toString();
		SchemaEntity entity = schemaDao.get(schemaUid)
				.orElseThrow(() -> new SchemaNotFoundException(
						String.format("Schema [%s] does not exists", name)
				));
		schemaDao.delete(entity);
	}

	public Optional<SchemaEntity> get(String schemaUid) {
		return schemaDao.get(schemaUid);
	}

	public SchemaEntity getOrElseThrow(String schemaUid) {
		return schemaDao.get(schemaUid)
				.orElseThrow(() -> new SchemaNotFoundException(
						String.format("Impossible to find schema with uri: '%s'", schemaUid)
				));
	}

	public List<String> getAllNamespaces() {
		return schemaDao.getAllNamespaces();
	}
}
