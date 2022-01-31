package com.sourcesense.joyce.core.configuration;

import com.sourcesense.joyce.protobuf.api.SchemaApiGrpc;
import lombok.extern.log4j.Log4j2;
import net.devh.boot.grpc.client.inject.GrpcClient;
import net.devh.boot.grpc.client.inject.GrpcClientBean;
import net.devh.boot.grpc.client.inject.GrpcClientBeans;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

@Log4j2
@Configuration
@GrpcClientBeans({
		@GrpcClientBean(beanName = "schemaStub", clazz = SchemaApiGrpc.SchemaApiBlockingStub.class, client = @GrpcClient("schema")),
		@GrpcClientBean(beanName = "asyncSchemaStub", clazz = SchemaApiGrpc.SchemaApiFutureStub.class, client = @GrpcClient("schema"))
})
@ConditionalOnProperty(value = "grpc.client.schema.enabled", havingValue = "true")
public class GrpcClientConfiguration {}

