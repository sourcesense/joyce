package com.sourcesense.nile.ingestion.core.errors;

import com.jayway.jsonpath.PathNotFoundException;
import com.sourcesense.nile.core.dto.ApiError;
import com.sourcesense.nile.core.errors.SchemaNotFoundException;
import com.sourcesense.nile.schemaengine.exceptions.InvalidSchemaVersion;
import org.apache.kafka.common.KafkaException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;

@ControllerAdvice(basePackages = "com.sourcesense.nile")
public class CustonExceptionHandler {

	@ExceptionHandler(value = InvalidSchemaVersion.class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	@ResponseBody
	public ApiError handler(InvalidSchemaVersion exception, WebRequest request) {
		return new ApiError(exception.getMessage(), exception.getClass().getCanonicalName());
	}

	@ExceptionHandler(value = PathNotFoundException.class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	@ResponseBody
	public ApiError jsonPathNotFound(PathNotFoundException exception, WebRequest request) {
		return new ApiError(exception.getMessage(), exception.getClass().getCanonicalName());
	}

	@ExceptionHandler(value = KafkaException.class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	@ResponseBody
	public ApiError kafkaException(KafkaException exception, WebRequest request) {
		return new ApiError(exception.getMessage(), exception.getClass().getCanonicalName());
	}

	@ExceptionHandler(value = MissingMetadataException.class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	@ResponseBody
	public ApiError handler(MissingMetadataException exception, WebRequest request) {
		return new ApiError(exception.getMessage(), exception.getClass().getCanonicalName());
	}

	@ExceptionHandler(value = SchemaNotFoundException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	@ResponseBody
	public ApiError schemaNotFound(SchemaNotFoundException exception, WebRequest request) {
		return new ApiError(exception.getMessage(), exception.getClass().getCanonicalName());
	}
}
