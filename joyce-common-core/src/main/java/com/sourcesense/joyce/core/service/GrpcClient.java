package com.sourcesense.joyce.core.service;

import com.google.common.collect.Lists;
import com.google.protobuf.Empty;
import com.google.protobuf.StringValue;
import com.sourcesense.joyce.core.exception.SchemaNotFoundException;
import com.sourcesense.joyce.core.mapping.mapper.SchemaProtoMapper;
import com.sourcesense.joyce.core.model.JoyceURI;
import com.sourcesense.joyce.core.model.SchemaEntity;
import com.sourcesense.joyce.protobuf.api.RequestParams;
import com.sourcesense.joyce.protobuf.api.SchemaApiGrpc;
import com.sourcesense.joyce.protobuf.model.OptionalSchema;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@DependsOn("grpcClientConfiguration")
@ConditionalOnProperty(value = "grpc.client.schema.enabled", havingValue = "true")
public class GrpcClient implements SchemaClient {

	private final SchemaProtoMapper schemaMapper;
	private final SchemaApiGrpc.SchemaApiBlockingStub schemaStub;

	@Override
	public Optional<SchemaEntity> get(String id) {
		StringValue request = StringValue.newBuilder().setValue(id).build();
		return Optional.ofNullable(schemaStub.getSchema(request))
				.map(OptionalSchema::getSchema)
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
		StringValue request = StringValue.newBuilder().setValue(Boolean.toString(rootOnly)).build();
		return schemaMapper.protosToEntities(
				schemaStub.getAllSchemas(request)
		);
	}

	@Override
	public List<SchemaEntity> getAllBySubtypeAndNamespace(JoyceURI.Subtype subtype, String namespace, Boolean rootOnly) {
		return schemaMapper.protosToEntities(schemaStub.getAllSchemasBySubtypeAndNamespace(
				RequestParams.newBuilder()
						.setSubtype(schemaMapper.joyceUriSubtypeEntityToProto(subtype))
						.setNamespace(namespace)
						.setRootOnly(Boolean.toString(rootOnly))
						.build()
		));
	}

	@Override
	public List<SchemaEntity> getAllByReportsIsNotEmpty() {
		return schemaMapper.protosToEntities(
				schemaStub.getAllSchemasByReportsIsNotEmpty(Empty.getDefaultInstance())
		);
	}

	@Override
	public List<String> getAllNamespaces() {
		return Lists.newArrayList(schemaStub.getAllNamespaces(Empty.getDefaultInstance())).stream()
				.map(StringValue::getValue)
				.collect(Collectors.toList());
	}
}
