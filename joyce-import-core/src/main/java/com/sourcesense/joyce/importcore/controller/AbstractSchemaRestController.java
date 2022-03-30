package com.sourcesense.joyce.importcore.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.sourcesense.joyce.core.model.dto.ConnectorOperationStatus;
import com.sourcesense.joyce.core.model.entity.JoyceSchemaMetadataExtraConnector;
import com.sourcesense.joyce.core.model.entity.SchemaEntity;
import com.sourcesense.joyce.core.model.entity.SchemaObject;
import com.sourcesense.joyce.importcore.service.ConnectorService;
import com.sourcesense.joyce.importcore.service.ValidationService;
import com.sourcesense.joyce.schemacore.api.SchemaRestApi;
import com.sourcesense.joyce.schemacore.mapper.SchemaDtoMapper;
import com.sourcesense.joyce.schemacore.model.dto.SchemaInfo;
import com.sourcesense.joyce.schemacore.model.dto.SchemaSave;
import com.sourcesense.joyce.schemacore.service.SchemaService;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public abstract class AbstractSchemaRestController implements SchemaRestApi {

	protected final SchemaDtoMapper schemaMapper;
	protected final SchemaService schemaService;
	protected final ConnectorService connectorService;
	protected final ValidationService validationService;

	//Todo: must be deleted and
	@Override
	public List<String> getAllNamespaces() {
		return schemaService.getAllNamespaces();
	}

	@Override
	public List<SchemaObject> getAllSchemas(
			Boolean fullSchema,
			Boolean rootOnly) {

		List<SchemaEntity> schemas = schemaService.getAll(rootOnly);
		return schemaMapper.entitiesToShortIfFullSchema(schemas, fullSchema);
	}

	public List<SchemaObject> getAllSchemasForDomainAndProduct(
			String domain,
			String product,
			Boolean fullSchema,
			Boolean rootOnly) {

		List<SchemaEntity> schemas = schemaService.getAllByDomainAndProduct(domain, product, rootOnly);
		return schemaMapper.entitiesToShortIfFullSchema(schemas, fullSchema);
	}

	@Override
	public SchemaEntity getSchema(String domain, String product, String name) {
		return schemaService.getOrElseThrow(domain, product, name);
	}

	@Override
	public SchemaInfo saveSchemaJson(SchemaSave schema) {
		return saveSchema(schema);
	}

	@Override
	public SchemaInfo saveSchemaYaml(SchemaSave schema) {
		return saveSchema(schema);
	}

	@Override
	public SchemaInfo deleteSchema(String domain, String product, String name) {
		List<ConnectorOperationStatus> connectors = connectorService.deleteConnectors(domain, product, name);
		schemaService.delete(domain, product, name);
		return SchemaInfo.builder().connectors(connectors).build();
	}

	@Override
	public List<JoyceSchemaMetadataExtraConnector> getConnectors(String domain, String product, String name) {
		return connectorService.getConnectors(domain, product, name);
	}

	@Override
	public JsonNode getConnectorStatus(String domain, String product, String name, String connector) {
		return connectorService.getConnectorStatus(domain, product, name, connector);
	}

	@Override
	public Boolean restartConnector(String domain, String product, String name, String connector) {
		return connectorService.restartConnector(domain, product, name, connector);
	}

	@Override
	public Boolean pauseConnector(String domain, String product, String name, String connector) {
		return connectorService.pauseConnector(domain, product, name, connector);
	}

	@Override
	public Boolean resumeConnector(String domain, String product, String name, String connector) {
		return connectorService.resumeConnector(domain, product, name, connector);
	}

	@Override
	public Boolean restartConnectorTask(String domain, String product, String name, String connector, String task) {
		return connectorService.restartConnectorTask(domain, product, name, connector, task);
	}

	protected SchemaInfo saveSchema(SchemaSave schema) {
		validationService.validateSchema(schema);
		return SchemaInfo.builder()
				.schemaUri(schemaService.save(schema))
				.connectors(connectorService.computeConnectors(schema))
				.build();
	}
}
