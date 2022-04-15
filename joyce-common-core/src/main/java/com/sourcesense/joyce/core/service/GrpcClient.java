package com.sourcesense.joyce.core.service;

import com.google.protobuf.Empty;
import com.sourcesense.joyce.core.exception.SchemaNotFoundException;
import com.sourcesense.joyce.core.mapping.mapstruct.SchemaProtoMapper;
import com.sourcesense.joyce.core.model.entity.SchemaEntity;
import com.sourcesense.joyce.core.model.uri.JoyceURIFactory;
import com.sourcesense.joyce.protobuf.api.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

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
	public Optional<SchemaEntity> get(String domain, String product, String name) {
		return this.get(
				JoyceURIFactory.getInstance().createSchemaURIOrElseThrow(domain, product, name).toString()
		);
	}

	@Override
	public SchemaEntity getOrElseThrow(String id) {
		return this.get(id)
				.orElseThrow(() -> new SchemaNotFoundException(
						String.format("Schema [%s] doesn not exist", id)
				));
	}

	@Override
	public SchemaEntity getOrElseThrow(String domain, String product, String name) {
		return this.get(domain, product, name)
				.orElseThrow(() -> new SchemaNotFoundException(
						String.format("Schema for [%s:%s:%s] doesn not exist", domain, product, name)
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
	public List<SchemaEntity> getAllByDomainAndProduct(String domain, String product, Boolean rootOnly) {
		GetAllSchemasBySubtypeAndNamespaceRequest request = this.buildGetAllSchemasBySubtypeAndNamespaceRequest(domain, product, rootOnly);
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
			String domain,
			String product,
			Boolean rootOnly) {

		return GetAllSchemasBySubtypeAndNamespaceRequest.newBuilder()
				.setDomain(domain)
				.setProduct(product)
				.setRootOnly(Boolean.toString(rootOnly))
				.build();
	}
}
