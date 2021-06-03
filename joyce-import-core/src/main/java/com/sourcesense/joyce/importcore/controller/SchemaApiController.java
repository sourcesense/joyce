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

package com.sourcesense.joyce.importcore.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sourcesense.joyce.core.api.SchemaApi;
import com.sourcesense.joyce.core.dto.Schema;
import com.sourcesense.joyce.core.dto.SchemaSave;
import com.sourcesense.joyce.core.dto.SchemaShort;
import com.sourcesense.joyce.core.exception.SchemaNotFoundException;
import com.sourcesense.joyce.core.model.JoyceURI;
import com.sourcesense.joyce.core.service.SchemaService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class SchemaApiController implements SchemaApi {

	final protected SchemaService schemaService;

	@Override
	public List<SchemaShort> getAllSchema() {
		return schemaService.findAll();
	}

	@Override
	public Schema getSchema(String name) {
		return this.getValidatedSchema(name);
	}

	@Override
	public JoyceURI saveSchemaJson(SchemaSave schema) throws JsonProcessingException {
		return saveSchema(schema);
	}

	@Override
	public JoyceURI saveSchemaYaml(SchemaSave schema) throws JsonProcessingException {
		return saveSchema(schema);
	}

	public JoyceURI saveSchema(SchemaSave schema) throws JsonProcessingException {
		JoyceURI uri = schemaService.save(schema);
		this.getValidatedSchema(uri.getCollection());
		return uri;
	}

	@Override
	public void deleteSchema(String name) {

		schemaService.delete(name);
	}

	private Schema getValidatedSchema(String name) {
		return schemaService.findByName(name)
				.orElseThrow(
						() -> new SchemaNotFoundException(String.format("Schema [%s] does not exists", name))
				);
	}
}
