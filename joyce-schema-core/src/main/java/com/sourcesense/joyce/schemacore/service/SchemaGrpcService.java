package com.sourcesense.joyce.schemacore.service;

import com.google.protobuf.Empty;
import com.sourcesense.joyce.core.mapping.mapstruct.SchemaProtoMapper;
import com.sourcesense.joyce.core.model.entity.SchemaEntity;
import com.sourcesense.joyce.protobuf.api.*;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;

import java.util.List;


@RequiredArgsConstructor
public abstract class SchemaGrpcService extends SchemaApiGrpc.SchemaApiImplBase implements GrpcService {

	protected final SchemaService schemaService;
	protected final SchemaProtoMapper schemaMapper;

	public void getSchema(GetSchemaRequest request, StreamObserver<GetSchemaResponse> responseObserver) {
		this.handleRequest(request, responseObserver, this::getSchema);
	}

	public void getAllSchemas(GetAllSchemasRequest request, StreamObserver<GetSchemasResponse> responseObserver) {
		this.handleRequest(request, responseObserver, this::getAllSchemas);
	}

	public void getAllSchemasByDomainAndProduct(GetAllSchemasByDomainAndProductRequest request, StreamObserver<GetSchemasResponse> responseObserver) {
		this.handleRequest(request, responseObserver, this::getAllSchemasByDomainAndProduct);
	}

	public void getAllSchemasByReportsIsNotEmpty(Empty request, StreamObserver<GetSchemasResponse> responseObserver) {
		this.handleRequest(request, responseObserver, this::getAllSchemasByReportsIsNotEmpty);
	}

	private GetSchemaResponse getSchema(GetSchemaRequest request) {
		return schemaService.get(request.getId())
				.map(schemaMapper::entityToProto)
				.map(GetSchemaResponse.newBuilder()::setSchema)
				.map(GetSchemaResponse.Builder::build)
				.orElse(null);
	}

	private GetSchemasResponse getAllSchemas(GetAllSchemasRequest request) {
		List<SchemaEntity> schemas = schemaService.getAll(
				Boolean.parseBoolean(request.getRootOnly())
		);
		return this.buildSchemasResponse(schemas);
	}

	private GetSchemasResponse getAllSchemasByDomainAndProduct(GetAllSchemasByDomainAndProductRequest request) {
		List<SchemaEntity> schemas = schemaService.getAllByDomainAndProduct(
				request.getDomain(),
				request.getProduct(),
				Boolean.parseBoolean(request.getRootOnly())
		);
		return this.buildSchemasResponse(schemas);
	}

	public GetSchemasResponse getAllSchemasByReportsIsNotEmpty(Empty empty) {
		List<SchemaEntity> schemas = schemaService.getAllByReportsIsNotEmpty();
		return this.buildSchemasResponse(schemas);
	}

	private GetSchemasResponse buildSchemasResponse(List<SchemaEntity> schemas) {
		return GetSchemasResponse.newBuilder()
				.addAllSchemas(schemaMapper.entitiesToProtos(schemas))
				.build();
	}
}
