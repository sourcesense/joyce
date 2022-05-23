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

package com.sourcesense.joyce.schemacore.service;

import com.sourcesense.joyce.core.exception.SchemaNotFoundException;
import com.sourcesense.joyce.core.model.entity.SchemaEntity;
import com.sourcesense.joyce.core.model.uri.JoyceSchemaURI;
import com.sourcesense.joyce.core.model.uri.JoyceURIFactory;
import com.sourcesense.joyce.core.producer.SchemaProducer;
import com.sourcesense.joyce.core.service.SchemaClient;
import com.sourcesense.joyce.schemacore.mapping.mapstruct.SchemaDtoMapper;
import com.sourcesense.joyce.schemacore.model.dto.SchemaSave;
import com.sourcesense.joyce.schemacore.model.entity.SchemaDocument;
import com.sourcesense.joyce.schemacore.repository.SchemaRepository;
import com.sourcesense.joyce.schemaengine.exception.JoyceSchemaEngineException;
import com.sourcesense.joyce.schemaengine.service.SchemaEngine;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Primary
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(value = "joyce.data.mongodb.enabled", havingValue = "true")
public class SchemaService implements SchemaClient {

	private final SchemaDtoMapper schemaMapper;
	private final SchemaProducer schemaProducer;
	private final SchemaRepository schemaRepository;
	private final SchemaEngine<SchemaEntity> schemaEngine;


	public Optional<SchemaEntity> get(String id) {
		return schemaRepository.findById(id).map(schemaMapper::entityFromDocument);
	}

	public Optional<SchemaEntity> get(String domain, String product, String name) {
		return this.get(
				JoyceURIFactory.getInstance().createSchemaURIOrElseThrow(domain, product, name).toString()
		);
	}

	public SchemaEntity getOrElseThrow(String id) {
		return this.get(id)
				.orElseThrow(() -> new SchemaNotFoundException(
						String.format("Schema [%s] does not exist", id)
				));
	}

	public SchemaEntity getOrElseThrow(String domain, String product, String name) {
		return this.get(domain, product, name)
				.orElseThrow(() -> new SchemaNotFoundException(
						String.format("Schema [%s/%s/%s] does not exist", domain, product, name))
				);
	}

	public List<SchemaEntity> getAll(Boolean rootOnly) {
		List<SchemaDocument> schemas = rootOnly
				? schemaRepository.findAllByMetadata_ParentIsNull()
				: schemaRepository.findAll();

		return schemaMapper.entitiesFromDocuments(schemas);
	}

	public List<SchemaEntity> getAllByDomainAndProduct(
			String domain,
			String product,
			Boolean rootOnly) {

		List<SchemaDocument> schemas = rootOnly
				? schemaRepository.findAllByMetadata_DomainAndMetadata_ProductAndMetadata_ParentIsNull(domain, product)
				: schemaRepository.findAllByMetadata_DomainAndMetadata_Product(domain, product);

		return schemaMapper.entitiesFromDocuments(schemas);
	}

	public List<SchemaEntity> getAllByReportsIsNotEmpty() {
		List<SchemaDocument> schemas = schemaRepository.findAllByReportsIsNotEmpty();
		return schemaMapper.entitiesFromDocuments(schemas);
	}

	public JoyceSchemaURI save(SchemaSave schema) {
		SchemaEntity schemaEntity = schemaMapper.toEntity(schema);

		JoyceSchemaURI schemaURI = JoyceURIFactory.getInstance().createSchemaURIOrElseThrow(
				schemaEntity.getMetadata().getDomain(),
				schemaEntity.getMetadata().getProduct(),
				schemaEntity.getMetadata().getName()
		);

		schemaEntity.setUid(schemaURI);

		// Validate schema
		schema.getMetadata().validate();

		// If schema has a parent it must exists
		this.validateParent(schemaEntity);

		// Looking for a previous version of the schema.
		//	If it exists, we check if there are breaking changes
		this.validateExisting(schemaEntity);

		SchemaDocument document = schemaMapper.documentFromEntity(schemaEntity);
		schemaRepository.save(document);
		schemaProducer.publish(schemaEntity);
		return schemaURI;
	}

	public void delete(String domain, String product, String name) {
		SchemaEntity schemaEntity = this.getOrElseThrow(domain, product, name);
		SchemaDocument document = schemaMapper.documentFromEntity(schemaEntity);
		schemaRepository.delete(document);
		schemaProducer.delete(schemaEntity);
	}

	private void validateParent(SchemaEntity schemaEntity) {
		if (schemaEntity.getMetadata().getParent() != null) {
			Optional<SchemaEntity> parentSchema = this.get(schemaEntity.getMetadata().getParent().toString());
			if (parentSchema.isEmpty()) {
				throw new JoyceSchemaEngineException(String.format("Parent schema [%s] does not exists", schemaEntity.getMetadata().getParent()));
			}
		}
	}

	private void validateExisting(SchemaEntity schemaEntity) {
		Optional<SchemaEntity> existingSchema = this.get(schemaEntity.getUid().toString());
		if (existingSchema.isPresent()) {
			/*
			 * If schema is in development mode we skip schema checks,
			 * but we block if previous schema is not in dev mode
			 */
			if (schemaEntity.getMetadata().getDevelopment() && !existingSchema.get().getMetadata().getDevelopment()) {
				throw new JoyceSchemaEngineException("Previous schema is not in development mode");
			}

			if (!schemaEntity.getMetadata().getDevelopment() && !existingSchema.get().getMetadata().getDevelopment()) {
				schemaEngine.checkForBreakingChanges(existingSchema.get(), schemaEntity);
			}
		}
	}
}
