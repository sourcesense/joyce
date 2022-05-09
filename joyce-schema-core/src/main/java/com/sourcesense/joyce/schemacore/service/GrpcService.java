package com.sourcesense.joyce.schemacore.service;

import com.google.protobuf.AbstractMessage;
import com.sourcesense.joyce.core.exception.handler.CustomExceptionHandler;
import io.grpc.stub.StreamObserver;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public interface GrpcService {

	CustomExceptionHandler customExceptionHandler = new CustomExceptionHandler();

	default <REQ extends AbstractMessage, RES extends AbstractMessage> void handleRequest(
			REQ request, StreamObserver<RES> responseObserver,
			Function<REQ, RES> requestHandler) {

		try {
			Optional.ofNullable(request)
					.map(requestHandler)
					.ifPresent(responseObserver::onNext);

		} catch (Exception exception) {
			customExceptionHandler.handleException(exception);
			responseObserver.onError(exception);
		} finally {
			responseObserver.onCompleted();
		}
	}
}
