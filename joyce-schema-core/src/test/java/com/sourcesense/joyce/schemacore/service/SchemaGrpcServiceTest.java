package com.sourcesense.joyce.schemacore.service;


import com.google.protobuf.Empty;
import com.sourcesense.joyce.core.mapping.mapstruct.ProtoConverter;
import com.sourcesense.joyce.core.mapping.mapstruct.SchemaProtoMapper;
import com.sourcesense.joyce.core.mapping.mapstruct.SchemaProtoMapperImpl;
import com.sourcesense.joyce.core.model.entity.SchemaEntity;
import com.sourcesense.joyce.protobuf.api.*;
import com.sourcesense.joyce.protobuf.model.Schema;
import com.sourcesense.joyce.schemacore.test.TestUtility;
import io.grpc.internal.testing.StreamRecorder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SchemaGrpcServiceTest implements TestUtility {

	private static final String DOMAIN = "test";
	private static final String PRODUCT = "default";
	private static final String FALSE = Boolean.toString(false);
	private static final String SCHEMA_URI = "joyce:content:test:default:user:schema";


	private SchemaProtoMapper schemaMapper;
	private SchemaGrpcService schemaGrpcService;

	@Mock
	private SchemaService schemaService;

	@BeforeEach
	public void setup() {
		schemaMapper = new SchemaProtoMapperImpl();
		schemaMapper.setJsonMapper(this.jsonMapper);
		schemaMapper.setProtoConverter(new ProtoConverter());
		schemaGrpcService = new SchemaGrpcService(schemaService, schemaMapper) {};
	}

	@Test
	public void shouldRetrieveSchemaFromGetSchema() throws IOException, URISyntaxException {
		SchemaEntity schemaEntity = this.computeSchemaEntity("schema.json");
		Schema schemaProto = this.computeSchemaProto(schemaEntity);

		GetSchemaRequest request = GetSchemaRequest.newBuilder().setId(SCHEMA_URI).build();
		StreamRecorder<GetSchemaResponse> responseObserver = StreamRecorder.create();

		when(schemaService.get(any())).thenReturn(Optional.of(schemaEntity));
		schemaGrpcService.getSchema(request, responseObserver);

		assertEquals(
				schemaProto,
				responseObserver.getValues().get(0).getSchema()
		);
	}

	@Test
	public void shouldRetrieveNullFromGetSchema() {
		GetSchemaRequest request = GetSchemaRequest.newBuilder().setId(SCHEMA_URI).build();
		StreamRecorder<GetSchemaResponse> responseObserver = StreamRecorder.create();

		when(schemaService.get(any())).thenReturn(Optional.empty());
		schemaGrpcService.getSchema(request, responseObserver);

		assertThat(responseObserver.getValues()).hasSize(0);
	}

	@Test
	public void shouldRetrieveSchemasFromGetAllSchemas() throws IOException, URISyntaxException {
		SchemaEntity schemaEntity = this.computeSchemaEntity("schema.json");

		List<SchemaEntity> schemaEntities = this.computeSchemaEntities(schemaEntity);
		List<Schema> schemaProtos = this.computeSchemaProtos(schemaEntity);

		StreamRecorder<GetSchemasResponse> responseObserver = StreamRecorder.create();
		GetAllSchemasRequest request = GetAllSchemasRequest.newBuilder().setRootOnly(FALSE).build();

		when(schemaService.getAll(false)).thenReturn(schemaEntities);
		schemaGrpcService.getAllSchemas(request, responseObserver);

		assertThat(schemaProtos).hasSameElementsAs(
				responseObserver.getValues().get(0).getSchemasList()
		);
	}

	@Test
	public void shouldRetrieveSchemasFromGetAllSchemasBySubtypeAndNamespace() throws IOException, URISyntaxException {
		SchemaEntity schemaEntity = this.computeSchemaEntity("schema.json");

		List<SchemaEntity> schemaEntities = this.computeSchemaEntities(schemaEntity);
		List<Schema> schemaProtos = this.computeSchemaProtos(schemaEntity);

		StreamRecorder<GetSchemasResponse> responseObserver = StreamRecorder.create();
		GetAllSchemasBySubtypeAndNamespaceRequest request = GetAllSchemasBySubtypeAndNamespaceRequest.newBuilder()
				.setDomain(DOMAIN)
				.setProduct(PRODUCT)
				.setRootOnly(FALSE)
				.build();

		when(schemaService.getAllByDomainAndProduct(any(), any(), any())).thenReturn(schemaEntities);
		schemaGrpcService.getAllSchemasBySubtypeAndNamespace(request, responseObserver);

		assertThat(schemaProtos).hasSameElementsAs(
				responseObserver.getValues().get(0).getSchemasList()
		);
	}

	@Test
	public void shouldRetrieveSchemasFromGetAllSchemasByReportsIsNotEmpty() throws IOException, URISyntaxException {
		SchemaEntity schemaEntity = this.computeSchemaEntity("schema.json");

		List<SchemaEntity> schemaEntities = this.computeSchemaEntities(schemaEntity);
		List<Schema> schemaProtos = this.computeSchemaProtos(schemaEntity);

		StreamRecorder<GetSchemasResponse> responseObserver = StreamRecorder.create();

		when(schemaService.getAllByReportsIsNotEmpty()).thenReturn(schemaEntities);
		schemaGrpcService.getAllSchemasByReportsIsNotEmpty(Empty.getDefaultInstance(), responseObserver);

		assertThat(schemaProtos).hasSameElementsAs(
				responseObserver.getValues().get(0).getSchemasList()
		);
	}

	private SchemaEntity computeSchemaEntity(String schemaPath) throws IOException, URISyntaxException {
		return this.computeResourceAsObject(schemaPath, SchemaEntity.class);
	}

	private Schema computeSchemaProto(SchemaEntity schemaEntity) {
		return schemaMapper.entityToProto(schemaEntity);
	}

	private List<SchemaEntity> computeSchemaEntities(SchemaEntity schemaEntity) {
		return Collections.singletonList(schemaEntity);
	}

	private List<Schema> computeSchemaProtos(SchemaEntity schemaEntity) {
		return Collections.singletonList(this.computeSchemaProto(schemaEntity));
	}
}
