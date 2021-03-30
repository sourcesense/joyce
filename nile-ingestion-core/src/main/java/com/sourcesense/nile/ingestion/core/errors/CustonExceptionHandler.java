package com.sourcesense.nile.ingestion.core.errors;

import com.jayway.jsonpath.PathNotFoundException;
import com.sourcesense.nile.ingestion.core.dto.ApiError;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;

@ControllerAdvice(basePackages = "com.sourcesense.nile")
public class CustonExceptionHandler {

	@ExceptionHandler(value = PathNotFoundException.class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	@ResponseBody
	public ApiError jsonPathNotFound(PathNotFoundException exception, WebRequest request) {
		return new ApiError(exception.getMessage(), exception.getClass().getCanonicalName());
	}


	@ExceptionHandler(value = SchemaNotFoundException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	@ResponseBody
	public ApiError schemaNotFound(SchemaNotFoundException exception, WebRequest request) {
		return new ApiError(exception.getMessage(), exception.getClass().getCanonicalName());
	}
}
