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

package com.sourcesense.nile.core.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sourcesense.nile.core.dto.Schema;
import com.sourcesense.nile.core.dto.SchemaSave;
import com.sourcesense.nile.core.dto.SchemaShort;
import com.sourcesense.nile.core.model.NileURI;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping( value = "/api/schema")
@Tag(name = "Schema API", description = "Schema	 Management API")
public interface SchemaApi {

	@GetMapping(produces = "application/json; charset=utf-8")
	@ResponseStatus(code = HttpStatus.OK)
	List<SchemaShort> getAllSchema();

	@GetMapping(value = "/{name}", produces = "application/json; charset=utf-8")
	@ResponseStatus(code = HttpStatus.OK)
	Schema getSchema(@PathVariable String name);

	@PostMapping(consumes = "application/json")
	@ResponseStatus(code = HttpStatus.CREATED)
    NileURI saveSchemaJson(@RequestBody SchemaSave schema) throws JsonProcessingException;

	@PostMapping(consumes = "application/x-yaml")
	@ResponseStatus(code = HttpStatus.CREATED)
    NileURI saveSchemaYaml(@RequestBody SchemaSave schema) throws JsonProcessingException;

	@DeleteMapping("/{name}")
	@ResponseStatus(code = HttpStatus.NO_CONTENT)
	void deleteSchema(@PathVariable String name);
}
