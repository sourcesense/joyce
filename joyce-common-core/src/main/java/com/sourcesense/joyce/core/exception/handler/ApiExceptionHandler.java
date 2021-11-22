package com.sourcesense.joyce.core.exception.handler;

import com.jayway.jsonpath.PathNotFoundException;
import com.sourcesense.joyce.core.dto.ApiError;
import com.sourcesense.joyce.core.exception.*;
import com.sourcesense.joyce.schemaengine.exception.InvalidSchemaException;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.common.KafkaException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;


@RequiredArgsConstructor
@ControllerAdvice(basePackages = {
		"com.sourcesense.joyce.core",
		"com.sourcesense.joyce.connectorcore",
		"com.sourcesense.joyce.connector",
		"com.sourcesense.joyce.importcore",

})
public class ApiExceptionHandler {

	private final CustomExceptionHandler exceptionHandler;

	@ResponseBody
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ExceptionHandler(InvalidSchemaException.class)
	public ApiError handler(InvalidSchemaException exception, WebRequest request) {
		return this.logExceptionAndComputeErrorResponse(exception);
	}

	@ResponseBody
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ExceptionHandler(InvalidMetadataException.class)
	public ApiError handler(InvalidMetadataException exception, WebRequest request) {
		return this.logExceptionAndComputeErrorResponse(exception);
	}

	@ResponseBody
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	@ExceptionHandler(PathNotFoundException.class)
	public ApiError handler(PathNotFoundException exception, WebRequest request) {
		return this.logExceptionAndComputeErrorResponse(exception);
	}

	@ResponseBody
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	@ExceptionHandler(RestException.class)
	public ApiError handler(RestException exception, WebRequest request) {
		return this.logExceptionAndComputeErrorResponse(exception);
	}

	@ResponseBody
	@ExceptionHandler(KafkaException.class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public ApiError handler(KafkaException exception, WebRequest request) {
		return this.logExceptionAndComputeErrorResponse(exception);
	}

	@ResponseBody
	@ResponseStatus(HttpStatus.NOT_FOUND)
	@ExceptionHandler(SchemaNotFoundException.class)
	public ApiError handler(SchemaNotFoundException exception, WebRequest request) {
		return this.logExceptionAndComputeErrorResponse(exception);
	}

	@ResponseBody
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	@ExceptionHandler(MappingValidationException.class)
	ApiError mappingValidationException(MappingValidationException exception) {
		return this.logExceptionAndComputeErrorResponse(exception);
	}

	@ResponseBody
	@ResponseStatus(HttpStatus.NOT_FOUND)
	@ExceptionHandler(DataInfoNotFoundException.class)
	ApiError dataInfoNotFoundException(DataInfoNotFoundException exception) {
		return this.logExceptionAndComputeErrorResponse(exception);
	}

	@ResponseBody
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	@ExceptionHandler(ProcessingException.class)
	ApiError processingException(ProcessingException exception) {
		return this.logExceptionAndComputeErrorResponse(exception);
	}

	@ResponseBody
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	@ExceptionHandler(Exception.class)
	ApiError exception(Exception exception) {
		return this.logExceptionAndComputeErrorResponse(exception);
	}

	private ApiError logExceptionAndComputeErrorResponse(Exception exception) {
		exceptionHandler.handleException(exception);
		return this.computeErrorResponse(exception);
	}

	private ApiError computeErrorResponse(Exception exception) {
		return ApiError.builder()
				.message(exception.getMessage())
				.error(exception.getClass().getCanonicalName())
				.build();
	}
}
