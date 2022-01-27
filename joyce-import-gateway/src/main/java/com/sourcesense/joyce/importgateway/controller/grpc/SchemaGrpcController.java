package com.sourcesense.joyce.importgateway.controller.grpc;

import com.sourcesense.joyce.core.mapping.mapper.SchemaProtoMapper;
import com.sourcesense.joyce.schemacore.service.SchemaGrpcService;
import com.sourcesense.joyce.schemacore.service.SchemaService;
import net.devh.boot.grpc.server.service.GrpcService;

@GrpcService
public class SchemaGrpcController extends SchemaGrpcService {

	public SchemaGrpcController(SchemaService schemaService, SchemaProtoMapper schemaMapper) {
		super(schemaService, schemaMapper);
	}
}
