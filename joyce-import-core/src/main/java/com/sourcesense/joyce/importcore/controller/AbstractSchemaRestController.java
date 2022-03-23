package com.sourcesense.joyce.importcore.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.sourcesense.joyce.core.model.dto.ConnectorOperationStatus;
import com.sourcesense.joyce.core.model.entity.JoyceSchemaMetadataExtraConnector;
import com.sourcesense.joyce.core.model.JoyceURI;
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

	@Override
	public List<String> getAllNamespaces() {
		return schemaService.getAllNamespaces();
	}

	@Override
	public List<SchemaObject> getAllSchema(
			Boolean fullSchema,
			Boolean rootOnly) {

		List<SchemaEntity> schemas = schemaService.getAll(rootOnly);
		return schemaMapper.entitiesToShortIfFullSchema(schemas, fullSchema);
	}

	@Override
	public List<SchemaObject> getAllSchemaForNamespace(
			String subtype,
			String namespace,
			Boolean fullSchema,
			Boolean rootOnly) {

		JoyceURI.Subtype uriSubtype = JoyceURI.Subtype.getOrElseThrow(subtype);
		List<SchemaEntity> schemas = schemaService.getAllBySubtypeAndNamespace(uriSubtype, namespace, rootOnly);
		return schemaMapper.entitiesToShortIfFullSchema(schemas, fullSchema);
	}

	@Override
	public SchemaEntity getSchema(String subtype, String namespace, String name) {
		JoyceURI.Subtype uriSubtype = JoyceURI.Subtype.getOrElseThrow(subtype);
		return schemaService.getOrElseThrow(uriSubtype, namespace, name);
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
	public SchemaInfo deleteSchema(String subtype, String namespace, String name) {
		List<ConnectorOperationStatus> connectors = connectorService.deleteConnectors(subtype, namespace, name);
		JoyceURI.Subtype uriSubtype = JoyceURI.Subtype.getOrElseThrow(subtype);
		schemaService.delete(uriSubtype, namespace,	name);
		return SchemaInfo.builder().connectors(connectors).build();
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

	protected SchemaInfo saveSchema(SchemaSave schema) {
		validationService.validateSchema(schema);
		return SchemaInfo.builder()
				.schemaUri(schemaService.save(schema))
				.connectors(connectorService.computeConnectors(schema))
				.build();
	}
}
