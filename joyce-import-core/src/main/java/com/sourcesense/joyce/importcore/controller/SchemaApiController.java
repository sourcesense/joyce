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
import com.fasterxml.jackson.databind.JsonNode;
import com.sourcesense.joyce.core.api.SchemaApi;
import com.sourcesense.joyce.core.dto.SaveSchemaStatus;
import com.sourcesense.joyce.core.dto.Schema;
import com.sourcesense.joyce.core.dto.SchemaSave;
import com.sourcesense.joyce.core.exception.SchemaNotFoundException;
import com.sourcesense.joyce.core.mapper.SchemaMapper;
import com.sourcesense.joyce.core.model.SchemaEntity;
import com.sourcesense.joyce.core.service.SchemaService;
import com.sourcesense.joyce.core.model.JoyceSchemaMetadataExtraConnector;
import com.sourcesense.joyce.importcore.service.ConnectorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class SchemaApiController implements SchemaApi {

	final private SchemaMapper schemaMapper;
	final private ConnectorService connectorService;

	final protected SchemaService schemaService;

	@Override
	public List<String> getAllNamespaces() {
		return schemaService.getAllNamespaces();
	}

	@Override
	public ResponseEntity<?> getAllSchema(Boolean fullSchema) {
		List<SchemaEntity> schemas = schemaService.findAll();
		return ResponseEntity.ok(this.computeSchemas(schemas, fullSchema));
	}

	@Override
	public ResponseEntity<?> getAllSchemaForNamespace(
			String subtype,
			String namespace,
			Boolean fullSchema) {

		List<SchemaEntity> schemas = schemaService.findBySubtypeAndNamespace(this.computeSubtype(subtype), namespace);
		return ResponseEntity.ok(this.computeSchemas(schemas, fullSchema));
	}

	@Override
	public Schema getSchema(String subtype, String namespace, String name) {
		return schemaService.findByName(this.computeSubtype(subtype), namespace, name)
				.orElseThrow(
						() -> new SchemaNotFoundException(String.format("Schema [%s] does not exists", name))
				);
	}

	@Override
	public SaveSchemaStatus saveSchemaJson(SchemaSave schema) throws JsonProcessingException {
		return saveSchema(schema);
	}

	@Override
	public SaveSchemaStatus saveSchemaYaml(SchemaSave schema) throws JsonProcessingException {
		return saveSchema(schema);
	}

	@Override
	public void deleteSchema(String subtype, String namespace, String name) {
		schemaService.delete(
				this.computeSubtype(subtype),
				namespace,
				name
		);
	}

	@Override
	public List<JoyceSchemaMetadataExtraConnector> getConnectors(
			String subtype,
			String namespace,
			String name) {

		return connectorService.getConnectors(subtype, namespace, name);
	}

	@Override
	public JsonNode getConnectorStatus(
			String subtype,
			String namespace,
			String name,
			String connector) {

		return connectorService.getConnectorStatus(namespace, name, connector);
	}

	@Override
	public Boolean restartConnector(
			String subtype,
			String namespace,
			String name,
			String connector) {

		return connectorService.restartConnector(namespace, name, connector);
	}

	@Override
	public Boolean pauseConnector(
			String subtype,
			String namespace,
			String name,
			String connector) {

		return connectorService.pauseConnector(namespace, name, connector);
	}

	@Override
	public Boolean resumeConnector(
			String subtype,
			String namespace,
			String name,
			String connector) {

		return connectorService.resumeConnector(namespace, name, connector);
	}

	@Override
	public Boolean restartConnectorTask(
			String subtype,
			String namespace,
			String name,
			String connector,
			String task) {

		return connectorService.restartConnectorTask(namespace, name, connector, task);
	}

	public SaveSchemaStatus saveSchema(SchemaSave schema) throws JsonProcessingException {
		return SaveSchemaStatus.builder()
				.schemaUri(schemaService.save(schema))
				.connectors(connectorService.saveOrUpdateConnectors(schema))
				.build();
	}

	private List<?> computeSchemas(List<SchemaEntity> schemas, Boolean fullSchema) {
		return schemas.stream()
				.map(fullSchema ? Function.identity() : schemaMapper::toDtoShort)
				.collect(Collectors.toList());
	}
}
