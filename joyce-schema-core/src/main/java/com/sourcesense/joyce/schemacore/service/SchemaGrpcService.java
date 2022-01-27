package com.sourcesense.joyce.schemacore.service;

import com.google.protobuf.Empty;
import com.google.protobuf.NullValue;
import com.google.protobuf.StringValue;
import com.sourcesense.joyce.core.mapping.mapper.SchemaProtoMapper;
import com.sourcesense.joyce.protobuf.api.RequestParams;
import com.sourcesense.joyce.protobuf.api.SchemaApiGrpc;
import com.sourcesense.joyce.protobuf.model.OptionalSchema;
import com.sourcesense.joyce.protobuf.model.Schema;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;


@RequiredArgsConstructor
public abstract class SchemaGrpcService extends SchemaApiGrpc.SchemaApiImplBase implements GrpcService {

	private final SchemaService schemaService;
	private final SchemaProtoMapper schemaMapper;

	public void getSchema(StringValue request, StreamObserver<OptionalSchema> responseObserver) {
		this.handleRequest(request, responseObserver, id -> Collections.singletonList(this.getSchema(id)));
	}

	public void getAllSchemas(StringValue request, StreamObserver<Schema> responseObserver) {
		this.handleRequest(request, responseObserver, this::getAllSchemas);
	}

	public void getAllSchemasBySubtypeAndNamespace(RequestParams request, StreamObserver<Schema> responseObserver) {
		this.handleRequest(request, responseObserver, this::getAllSchemasBySubtypeAndNamespace);
	}

	public void getAllSchemasByReportsIsNotEmpty(Empty request, StreamObserver<Schema> responseObserver) {
		this.handleRequest(request, responseObserver, this::getAllSchemasByReportsIsNotEmpty);
	}

	public void getAllNamespaces(Empty request, StreamObserver<StringValue> responseObserver) {
		this.handleRequest(request, responseObserver, this::getAllNamespaces);
	}

	private OptionalSchema getSchema(StringValue id) {
		return schemaService.get(id.getValue())
				.map(schemaMapper::entityToProto)
				.map(schema -> OptionalSchema.newBuilder().setSchema(schema).build())
				.orElseGet(() -> OptionalSchema.newBuilder().setNull(NullValue.NULL_VALUE).build());
	}

	private List<Schema> getAllSchemas(StringValue rootOnly) {
		return schemaMapper.entitiesToProtos(schemaService.getAll(
				Boolean.parseBoolean(rootOnly.getValue())
		));
	}

	private List<Schema> getAllSchemasBySubtypeAndNamespace(RequestParams requestParams) {
		return schemaMapper.entitiesToProtos(schemaService.getAllBySubtypeAndNamespace(
				schemaMapper.joyceUriSubtypeProtoToEntity(requestParams.getSubtype()),
				requestParams.getNamespace(),
				Boolean.parseBoolean(requestParams.getRootOnly())
		));
	}

	public List<Schema> getAllSchemasByReportsIsNotEmpty(Empty empty) {
		return schemaMapper.entitiesToProtos(
				schemaService.getAllByReportsIsNotEmpty()
		);
	}

	public List<StringValue> getAllNamespaces(Empty request) {
		return schemaService.getAllNamespaces().stream()
				.map(namespace -> StringValue.newBuilder().setValue(namespace).build())
				.collect(Collectors.toList());
	}
}
