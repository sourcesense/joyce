/*
 *  Copyright 2021 Sourcesense Spa
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

package com.sourcesense.joyce.core.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sourcesense.joyce.core.dto.Schema;
import com.sourcesense.joyce.core.dto.SchemaSave;
import com.sourcesense.joyce.core.dto.SchemaShort;
import com.sourcesense.joyce.core.exception.InvalidMetadataException;
import com.sourcesense.joyce.core.model.JoyceURI;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping(value = "/api/schema")
@Tag(name = "Schema API", description = "Schema	 Management API")
public interface SchemaApi {

	@GetMapping(produces = "application/json; charset=utf-8")
	@ResponseStatus(code = HttpStatus.OK)
	List<SchemaShort> getAllSchema();

	@GetMapping(value = "/{subtype}/{namespace}", produces = "application/json; charset=utf-8")
	@ResponseStatus(code = HttpStatus.OK)
	List<SchemaShort> getAllSchemaForNamespace(@PathVariable String subtype, @PathVariable String namespace);

	@GetMapping(value = "/{subtype}/{namespace}/{name}", produces = "application/json; charset=utf-8")
	@ResponseStatus(code = HttpStatus.OK)
	Schema getSchema(@PathVariable String subtype, @PathVariable String namespace, @PathVariable String name);

	@DeleteMapping("/{subtype}/{namespace}/{name}")
	@ResponseStatus(code = HttpStatus.NO_CONTENT)
	void deleteSchema(@PathVariable String subtype, @PathVariable String namespace, @PathVariable String name);

	@PostMapping(consumes = "application/json")
	@ResponseStatus(code = HttpStatus.CREATED)
	JoyceURI saveSchemaJson(@RequestBody SchemaSave schema) throws JsonProcessingException;

	@PostMapping(consumes = "application/x-yaml")
	@ResponseStatus(code = HttpStatus.CREATED)
	JoyceURI saveSchemaYaml(@RequestBody SchemaSave schema) throws JsonProcessingException;

	default JoyceURI.Subtype computeSubtype(String subtype) {
		return JoyceURI.Subtype.get(subtype)
				.orElseThrow(
						() -> new InvalidMetadataException("Subtype is not valid: " + subtype)
				);
	}
}
