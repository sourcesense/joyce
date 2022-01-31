package com.sourcesense.joyce.schemacore.exception.handler;

import com.sourcesense.joyce.core.exception.InvalidMetadataException;
import com.sourcesense.joyce.core.exception.ProcessingException;
import com.sourcesense.joyce.core.exception.SchemaNotFoundException;
import com.sourcesense.joyce.core.exception.handler.CustomExceptionHandler;
import com.sourcesense.joyce.schemaengine.exception.InvalidSchemaException;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.advice.GrpcAdvice;
import net.devh.boot.grpc.server.advice.GrpcExceptionHandler;
import org.apache.kafka.common.KafkaException;

@GrpcAdvice
@RequiredArgsConstructor
public class GrpcErrorHandler {

	private final CustomExceptionHandler exceptionHandler;

	@GrpcExceptionHandler(InvalidSchemaException.class)
	public StatusRuntimeException handler(InvalidSchemaException exception) {
		return this.logExceptionAndComputeErrorResponse(exception, Status.INVALID_ARGUMENT);
	}

	@GrpcExceptionHandler(InvalidMetadataException.class)
	public StatusRuntimeException handler(InvalidMetadataException exception) {
		return this.logExceptionAndComputeErrorResponse(exception, Status.INVALID_ARGUMENT);
	}

	@GrpcExceptionHandler(KafkaException.class)
	public StatusRuntimeException handler(KafkaException exception) {
		return this.logExceptionAndComputeErrorResponse(exception, Status.INTERNAL);
	}

	@GrpcExceptionHandler(SchemaNotFoundException.class)
	public StatusRuntimeException handler(SchemaNotFoundException exception) {
		return this.logExceptionAndComputeErrorResponse(exception, Status.NOT_FOUND);
	}

	@GrpcExceptionHandler(ProcessingException.class)
	public StatusRuntimeException handler(ProcessingException exception) {
		return this.logExceptionAndComputeErrorResponse(exception, Status.INTERNAL);
	}

	@GrpcExceptionHandler(Exception.class)
	public StatusRuntimeException handler(Exception exception) {
		return this.logExceptionAndComputeErrorResponse(exception, Status.INTERNAL);
	}

	private StatusRuntimeException logExceptionAndComputeErrorResponse(Exception exception, Status status) {
		exceptionHandler.handleException(exception);
		return this.computeErrorResponse(exception, status);
	}

	private StatusRuntimeException computeErrorResponse(Exception exception, Status status) {
		return status.withDescription(exception.getMessage())
				.withCause(exception)
				.asRuntimeException();
	}
}
