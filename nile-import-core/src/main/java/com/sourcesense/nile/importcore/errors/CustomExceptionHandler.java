/*
 * Copyright 2021 Sourcesense Spa
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sourcesense.nile.importcore.errors;

import com.jayway.jsonpath.PathNotFoundException;
import com.sourcesense.nile.core.dto.ApiError;
import com.sourcesense.nile.core.exceptions.InvalidMetadataException;
import com.sourcesense.nile.core.exceptions.SchemaNotFoundException;
import com.sourcesense.nile.schemaengine.exceptions.InvalidSchemaException;
import com.sourcesense.nile.schemaengine.exceptions.SchemaIsNotValidException;
import org.apache.kafka.common.KafkaException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;

@ControllerAdvice(basePackages = "com.sourcesense.nile")
public class CustomExceptionHandler {

	@ExceptionHandler(value = InvalidSchemaException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ResponseBody
	public ApiError handler(InvalidSchemaException exception, WebRequest request) {
		return new ApiError(exception.getMessage(), exception.getClass().getCanonicalName());
	}

	@ExceptionHandler(value = InvalidMetadataException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ResponseBody
	public ApiError handler(InvalidMetadataException exception, WebRequest request) {
		return new ApiError(exception.getMessage(), exception.getClass().getCanonicalName());
	}

	@ExceptionHandler(value = PathNotFoundException.class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	@ResponseBody
	public ApiError handler(PathNotFoundException exception, WebRequest request) {
		return new ApiError(exception.getMessage(), exception.getClass().getCanonicalName());
	}

	@ExceptionHandler(value = KafkaException.class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	@ResponseBody
	public ApiError handler(KafkaException exception, WebRequest request) {
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
	public ApiError handler(SchemaNotFoundException exception, WebRequest request) {
		return new ApiError(exception.getMessage(), exception.getClass().getCanonicalName());
	}

	@ExceptionHandler(value = SchemaIsNotValidException.class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	@ResponseBody
	public ApiError handler(SchemaIsNotValidException exception, WebRequest request) {
		return new ApiError(exception.getMessage(), exception.getClass().getCanonicalName());
	}
}
