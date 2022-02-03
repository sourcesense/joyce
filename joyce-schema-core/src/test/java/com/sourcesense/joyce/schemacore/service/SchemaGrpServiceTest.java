package com.sourcesense.joyce.schemacore.service;


import com.google.protobuf.Empty;
import com.google.protobuf.NullValue;
import com.google.protobuf.StringValue;
import com.sourcesense.joyce.core.mapping.mapper.SchemaProtoMapper;
import com.sourcesense.joyce.core.model.JoyceURI;
import com.sourcesense.joyce.core.model.SchemaEntity;
import com.sourcesense.joyce.protobuf.api.RequestParams;
import com.sourcesense.joyce.protobuf.enumeration.JoyceUriSubtype;
import com.sourcesense.joyce.protobuf.model.OptionalSchema;
import com.sourcesense.joyce.protobuf.model.Schema;
import io.grpc.internal.testing.StreamRecorder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SchemaGrpServiceTest {

	private static final SchemaEntity SCHEMA_ENTITY = new SchemaEntity();
	private static final Schema SCHEMA_PROTO = Schema.getDefaultInstance();

	private static final List<SchemaEntity> SCHEMA_ENTITIES = Collections.singletonList(SCHEMA_ENTITY);
	private static final List<Schema> SCHEMA_PROTOS = Collections.singletonList(SCHEMA_PROTO);

	private static final String SCHEMA_ID_NAMESPACE = "default";
	private static final StringValue SCHEMA_ID_NAMESPACE_PROTO = StringValue.newBuilder().setValue(SCHEMA_ID_NAMESPACE).build();

	private static final List<String> SCHEMA_ID_NAMESPACES = Collections.singletonList(SCHEMA_ID_NAMESPACE);
	private static final List<StringValue> SCHEMA_ID_NAMESPACE_PROTOS = Collections.singletonList(SCHEMA_ID_NAMESPACE_PROTO);

	private static final String SCHEMA_ID = "joyce:/schema/import/default.test";
	private static final StringValue SCHEMA_ID_PROTO = StringValue.newBuilder().setValue(SCHEMA_ID).build();

	private static final String FALSE = Boolean.toString(false);
	private static final StringValue FALSE_PROTO = StringValue.newBuilder().setValue(FALSE).build();

	private SchemaGrpcService schemaGrpcService;

	@Mock
	private SchemaService schemaService;

	@Mock
	private SchemaProtoMapper schemaMapper;

	@BeforeEach
	public void setup() {
		schemaGrpcService = new SchemaGrpcService(schemaService, schemaMapper) {};
	}

	@Test
	public void shouldRetrieveSchemaFromGetSchema() {
		StreamRecorder<OptionalSchema> responseObserver = StreamRecorder.create();

		when(schemaService.get(any())).thenReturn(Optional.of(SCHEMA_ENTITY));
		when(schemaMapper.entityToProto(any())).thenReturn(SCHEMA_PROTO);

		schemaGrpcService.getSchema(SCHEMA_ID_PROTO, responseObserver);

		OptionalSchema expected = OptionalSchema.newBuilder().setSchema(SCHEMA_PROTO).build();
		OptionalSchema actual = responseObserver.getValues().get(0);

		assertEquals(expected, actual);
	}

	@Test
	public void shouldRetrieveNullValueFromGetSchema() {
		StreamRecorder<OptionalSchema> responseObserver = StreamRecorder.create();

		when(schemaService.get(any())).thenReturn(Optional.empty());

		schemaGrpcService.getSchema(SCHEMA_ID_PROTO, responseObserver);

		OptionalSchema expected = OptionalSchema.newBuilder().setNull(NullValue.NULL_VALUE).build();
		OptionalSchema actual = responseObserver.getValues().get(0);

		assertEquals(expected, actual);
	}

	@Test
	public void shouldRetrieveSchemasFromGetAllSchemas() {
		StreamRecorder<Schema> responseObserver = StreamRecorder.create();

		when(schemaService.getAll(false)).thenReturn(SCHEMA_ENTITIES);
		when(schemaMapper.entitiesToProtos(any())).thenReturn(SCHEMA_PROTOS);

		schemaGrpcService.getAllSchemas(FALSE_PROTO, responseObserver);

		List<Schema> expected = SCHEMA_PROTOS;
		List<Schema> actual = responseObserver.getValues();

		assertThat(expected).hasSameElementsAs(actual);
	}

	@Test
	public void shouldRetrieveSchemasFromGetAllSchemasBySubtypeAndNamespace() {
		StreamRecorder<Schema> responseObserver = StreamRecorder.create();
		RequestParams requestParams = RequestParams.newBuilder()
				.setSubtype(JoyceUriSubtype.IMPORT)
				.setNamespace(SCHEMA_ID_NAMESPACE)
				.setRootOnly(FALSE)
				.build();

		when(schemaMapper.joyceUriSubtypeProtoToEntity(JoyceUriSubtype.IMPORT)).thenReturn(JoyceURI.Subtype.IMPORT);
		when(schemaService.getAllBySubtypeAndNamespace(any(), any(), any())).thenReturn(SCHEMA_ENTITIES);
		when(schemaMapper.entitiesToProtos(any())).thenReturn(SCHEMA_PROTOS);

		schemaGrpcService.getAllSchemasBySubtypeAndNamespace(requestParams, responseObserver);

		List<Schema> expected = SCHEMA_PROTOS;
		List<Schema> actual = responseObserver.getValues();

		assertThat(expected).hasSameElementsAs(actual);
	}

	@Test
	public void shouldRetrieveSchemasFromGetAllSchemasByReportsIsNotEmpty() {
		StreamRecorder<Schema> responseObserver = StreamRecorder.create();

		when(schemaService.getAllByReportsIsNotEmpty()).thenReturn(SCHEMA_ENTITIES);
		when(schemaMapper.entitiesToProtos(any())).thenReturn(SCHEMA_PROTOS);

		schemaGrpcService.getAllSchemasByReportsIsNotEmpty(Empty.getDefaultInstance(), responseObserver);

		List<Schema> expected = SCHEMA_PROTOS;
		List<Schema> actual = responseObserver.getValues();

		assertThat(expected).hasSameElementsAs(actual);
	}

	@Test
	public void shouldRetrieveNamespacesFromGetAllNamespaces() {
		StreamRecorder<StringValue> responseObserver = StreamRecorder.create();

		when(schemaService.getAllNamespaces()).thenReturn(SCHEMA_ID_NAMESPACES);

		schemaGrpcService.getAllNamespaces(Empty.getDefaultInstance(), responseObserver);

		List<StringValue> expected = SCHEMA_ID_NAMESPACE_PROTOS;
		List<StringValue> actual = responseObserver.getValues();

		assertThat(expected).hasSameElementsAs(actual);
	}
}
