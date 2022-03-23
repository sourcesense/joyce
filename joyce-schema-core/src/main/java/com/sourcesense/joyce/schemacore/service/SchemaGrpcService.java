package com.sourcesense.joyce.schemacore.service;

import com.google.protobuf.Empty;
import com.sourcesense.joyce.core.mapping.mapper.SchemaProtoMapper;
import com.sourcesense.joyce.core.model.entity.SchemaEntity;
import com.sourcesense.joyce.protobuf.api.*;
import com.sourcesense.joyce.protobuf.model.Schema;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;

import java.util.List;


@RequiredArgsConstructor
public abstract class SchemaGrpcService extends SchemaApiGrpc.SchemaApiImplBase implements GrpcService {

	protected final SchemaService schemaService;
	protected final SchemaProtoMapper schemaMapper;

	public void getSchema(
			GetSchemaRequest request,
			StreamObserver<GetSchemaResponse> responseObserver) {

		this.handleRequest(request, responseObserver, this::getSchema);
	}

	public void getAllSchemas(
			GetAllSchemasRequest request,
			StreamObserver<GetSchemasResponse> responseObserver) {

		this.handleRequest(request, responseObserver, this::getAllSchemas);
	}

	public void getAllSchemasBySubtypeAndNamespace(
			GetAllSchemasBySubtypeAndNamespaceRequest request,
			StreamObserver<GetSchemasResponse> responseObserver) {

		this.handleRequest(request, responseObserver, this::getAllSchemasBySubtypeAndNamespace);
	}

	public void getAllSchemasByReportsIsNotEmpty(
			Empty request,
			StreamObserver<GetSchemasResponse> responseObserver) {

		this.handleRequest(request, responseObserver, this::getAllSchemasByReportsIsNotEmpty);
	}

	public void getAllNamespaces(
			Empty request,
			StreamObserver<GetNamespacesResponse> responseObserver) {

		this.handleRequest(request, responseObserver, this::getAllNamespaces);
	}

	private GetSchemaResponse getSchema(GetSchemaRequest request) {
		return schemaService.get(request.getId())
				.map(schemaMapper::entityToProto)
				.map(GetSchemaResponse.newBuilder()::setSchema)
				.map(GetSchemaResponse.Builder::build)
				.orElseGet(() -> GetSchemaResponse.newBuilder().setSchema((Schema) null).build());
	}

	private GetSchemasResponse getAllSchemas(GetAllSchemasRequest request) {
		List<SchemaEntity> schemas = schemaService.getAll(
				Boolean.parseBoolean(request.getRootOnly())
		);
		return this.buildSchemasResponse(schemas);
	}

	private GetSchemasResponse getAllSchemasBySubtypeAndNamespace(GetAllSchemasBySubtypeAndNamespaceRequest request) {
		List<SchemaEntity> schemas = schemaService.getAllBySubtypeAndNamespace(
				schemaMapper.joyceUriSubtypeProtoToEntity(request.getSubtype()),
				request.getNamespace(),
				Boolean.parseBoolean(request.getRootOnly())
		);
		return this.buildSchemasResponse(schemas);
	}

	public GetSchemasResponse getAllSchemasByReportsIsNotEmpty(Empty empty) {
		List<SchemaEntity> schemas = schemaService.getAllByReportsIsNotEmpty();
		return this.buildSchemasResponse(schemas);
	}

	public GetNamespacesResponse getAllNamespaces(Empty request) {
		List<String> namespaces = schemaService.getAllNamespaces();
		return this.buildNamespacesResponse(namespaces);
	}

	private GetSchemasResponse buildSchemasResponse(List<SchemaEntity> schemas) {
		return GetSchemasResponse.newBuilder()
				.addAllSchemas(schemaMapper.entitiesToProtos(schemas))
				.build();
	}

	private GetNamespacesResponse buildNamespacesResponse(List<String> namespaces) {
		return GetNamespacesResponse.newBuilder()
				.addAllNamespaces(namespaces)
				.build();
	}
}
