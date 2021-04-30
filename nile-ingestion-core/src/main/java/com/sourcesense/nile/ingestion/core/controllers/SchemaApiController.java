/*
 * Copyright 2021 Sourcesense Spa
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

package com.sourcesense.nile.ingestion.core.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sourcesense.nile.core.api.SchemaApi;
import com.sourcesense.nile.core.dto.Schema;
import com.sourcesense.nile.core.dto.SchemaSave;
import com.sourcesense.nile.core.dto.SchemaShort;
import com.sourcesense.nile.core.exceptions.SchemaNotFoundException;
import com.sourcesense.nile.core.model.NileURI;
import com.sourcesense.nile.core.service.SchemaService;
import com.sourcesense.nile.ingestion.core.service.IngestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class SchemaApiController implements SchemaApi {
	final protected SchemaService schemaService;
	final protected IngestionService ingestionService;

	@Override
	public List<SchemaShort> getAllSchema() {
		return schemaService.findAll();
	}

	@Override
	public Schema getSchema(String name) {
		Optional<Schema> schema = schemaService.findByName(name);
		if (schema.isEmpty()){
			throw new SchemaNotFoundException(String.format("Schema [%s] does not exists", name));
		}
		return schema.get();
	}

	@Override
	public List<SchemaShort> getSchemaWithVersions(String name) {
		List<SchemaShort> schemas = schemaService.getAllVersions(name);
		if (schemas.size() < 1){
			throw new SchemaNotFoundException(String.format("Schema [%s] does not exists", name));
		}
		return schemas;
	}

	@Override
	public Schema getSchemaVersion(String name, Integer version) {
		Optional<Schema> schema = schemaService.findByNameAndVersion(name, version);
		if (schema.isEmpty()){
			throw new SchemaNotFoundException(String.format("Schema [%s] v%d does not exists", name, version));
		}
		return schema.get();
	}

	@Override
	public NileURI saveSchemaJson(SchemaSave schema) throws JsonProcessingException {
		return saveSchema(schema);
	}

	@Override
	public NileURI saveSchemaYaml(SchemaSave schema) throws JsonProcessingException {
		return saveSchema(schema);
	}

	public NileURI saveSchema(SchemaSave schema) throws JsonProcessingException {
		NileURI uri = schemaService.save(schema);
		Optional<Schema> saved = schemaService.findByName(uri.getCollection());
		if (saved.isEmpty()){
			throw new SchemaNotFoundException(String.format("Schema [%s] v%d does not exists", uri.getCollection()));
		}
		ingestionService.publishSchema(saved.get());
		return uri;
	}

	@Override
	public void deleteSchema(String id) {
		// TODO: publish schema deletion to mainlog
		schemaService.delete(id);
	}
}
