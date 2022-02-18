package com.sourcesense.joyce.schemacore.service;

import com.google.protobuf.AbstractMessage;
import com.sourcesense.joyce.core.exception.handler.CustomExceptionHandler;
import io.grpc.stub.StreamObserver;

import java.util.List;
import java.util.function.Function;

public interface GrpcService {

	CustomExceptionHandler customExceptionHandler = new CustomExceptionHandler();

	default <REQ extends AbstractMessage, RES extends AbstractMessage> void handleRequest(
			REQ request, StreamObserver<RES> responseObserver,
			Function<REQ, RES> requestHandler) {

		try {
			RES response = requestHandler.apply(request);
			responseObserver.onNext(response);

		} catch (Exception exception) {
			customExceptionHandler.handleException(exception);
			responseObserver.onError(exception);
		} finally {
			responseObserver.onCompleted();
		}
	}
}
