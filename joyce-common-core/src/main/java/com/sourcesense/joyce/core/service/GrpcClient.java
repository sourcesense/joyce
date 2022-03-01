package com.sourcesense.joyce.core.service;

import com.google.protobuf.Empty;
import com.sourcesense.joyce.core.exception.SchemaNotFoundException;
import com.sourcesense.joyce.core.mapping.mapper.SchemaProtoMapper;
import com.sourcesense.joyce.core.model.JoyceURI;
import com.sourcesense.joyce.core.model.SchemaEntity;
import com.sourcesense.joyce.protobuf.api.*;
import com.sourcesense.joyce.protobuf.model.Schema;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

@Service
@RequiredArgsConstructor
@DependsOn("grpcClientConfiguration")
@ConditionalOnProperty(value = "grpc.client.schema.enabled", havingValue = "true")
public class GrpcClient implements SchemaClient {

	private final SchemaProtoMapper schemaMapper;
	private final SchemaApiGrpc.SchemaApiBlockingStub schemaStub;

	@Override
	public Optional<SchemaEntity> get(String id) {
		GetSchemaRequest request = GetSchemaRequest.newBuilder().setId(id).build();
		return Optional.ofNullable(schemaStub.getSchema(request))
				.map(GetSchemaResponse::getSchema)
				.map(schemaMapper::protoToEntity);
	}

	@Override
	public Optional<SchemaEntity> get(JoyceURI.Subtype subtype, String namespace, String name) {
		JoyceURI schemaUri = JoyceURI.makeNamespaced(JoyceURI.Type.SCHEMA, subtype, namespace, name);
		return this.get(schemaUri.toString());
	}

	@Override
	public SchemaEntity getOrElseThrow(String id) {
		return this.get(id)
				.orElseThrow(() -> new SchemaNotFoundException(
						String.format("Schema [%s] doesn not exist", id)
				));
	}

	@Override
	public SchemaEntity getOrElseThrow(JoyceURI.Subtype subtype, String namespace, String name) {
		JoyceURI schemaUri = JoyceURI.makeNamespaced(JoyceURI.Type.SCHEMA, subtype, namespace, name);
		return this.get(schemaUri.toString())
				.orElseThrow(() -> new SchemaNotFoundException(
						String.format("Schema [%s] doesn not exist", schemaUri)
				));
	}

	@Override
	public List<SchemaEntity> getAll(Boolean rootOnly) {
		GetAllSchemasRequest request = this.buildGetAllSchemasRequest(rootOnly);
		return schemaMapper.protosToEntities(
				schemaStub.getAllSchemas(request).getSchemasList()
		);
	}

	@Override
	public List<SchemaEntity> getAllBySubtypeAndNamespace(JoyceURI.Subtype subtype, String namespace, Boolean rootOnly) {
		GetAllSchemasBySubtypeAndNamespaceRequest request = this.buildGetAllSchemasBySubtypeAndNamespaceRequest(subtype, namespace, rootOnly);
		return schemaMapper.protosToEntities(
				schemaStub.getAllSchemasBySubtypeAndNamespace(request).getSchemasList()
		);
	}

	@Override
	public List<SchemaEntity> getAllByReportsIsNotEmpty() {
		Empty request = Empty.getDefaultInstance();
		return schemaMapper.protosToEntities(
				schemaStub.getAllSchemasByReportsIsNotEmpty(request).getSchemasList()
		);
	}

	@Override
	public List<String> getAllNamespaces() {
		Empty request = Empty.getDefaultInstance();
		return schemaStub.getAllNamespaces(request).getNamespacesList();
	}

	private GetAllSchemasRequest buildGetAllSchemasRequest(Boolean rootOnly) {
		return GetAllSchemasRequest.newBuilder()
				.setRootOnly(Boolean.toString(rootOnly))
				.build();
	}

	private GetAllSchemasBySubtypeAndNamespaceRequest buildGetAllSchemasBySubtypeAndNamespaceRequest(
			JoyceURI.Subtype subtype,
			String namespace,
			Boolean rootOnly) {

		return GetAllSchemasBySubtypeAndNamespaceRequest.newBuilder()
				.setSubtype(schemaMapper.joyceUriSubtypeEntityToProto(subtype))
				.setNamespace(namespace)
				.setRootOnly(Boolean.toString(rootOnly))
				.build();
	}
}
