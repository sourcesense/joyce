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
import com.fasterxml.jackson.databind.JsonNode;
import com.sourcesense.joyce.core.dto.SchemaInfo;
import com.sourcesense.joyce.core.dto.Schema;
import com.sourcesense.joyce.core.dto.SchemaSave;
import com.sourcesense.joyce.core.dto.SchemaShort;
import com.sourcesense.joyce.core.exception.InvalidMetadataException;
import com.sourcesense.joyce.core.model.JoyceSchemaMetadataExtraConnector;
import com.sourcesense.joyce.core.model.JoyceURI;
import com.sourcesense.joyce.core.model.SchemaEntity;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping(value = "/api")
@Tag(name = "Schema API", description = "Schema	 Management API")
public interface SchemaApi {

	@GetMapping(value = "/namespace", produces = "application/json; charset=utf-8")
	List<String> getAllNamespaces();

	@GetMapping(value = "/schema", produces = "application/json; charset=utf-8")
	@ApiResponse(
			responseCode = "200",
			content = @Content(
					schema = @io.swagger.v3.oas.annotations.media.Schema(
							anyOf = {
									SchemaShort.class,
									SchemaEntity.class
							}
					)
			)
	)
	ResponseEntity<?> getAllSchema(@RequestParam(defaultValue = "false") Boolean fullSchema);

	@GetMapping(value = "/schema/{subtype}/{namespace}", produces = "application/json; charset=utf-8")
	@ApiResponse(
			responseCode = "200",
			content = @Content(
					schema = @io.swagger.v3.oas.annotations.media.Schema(
							anyOf = {
									SchemaShort.class,
									SchemaEntity.class
							}
					)
			)
	)
	ResponseEntity<?> getAllSchemaForNamespace(
			@PathVariable String subtype,
			@PathVariable String namespace,
			@RequestParam(defaultValue = "false") Boolean fullSchema
	);

	@GetMapping(value = "/schema/{subtype}/{namespace}/{name}", produces = "application/json; charset=utf-8")
	@ResponseStatus(code = HttpStatus.OK)
	Schema getSchema(
			@PathVariable String subtype,
			@PathVariable String namespace,
			@PathVariable String name
	);

	@DeleteMapping("/schema/{subtype}/{namespace}/{name}")
	@ResponseStatus(code = HttpStatus.ACCEPTED)
	SchemaInfo deleteSchema(
			@PathVariable String subtype,
			@PathVariable String namespace,
			@PathVariable String name
	);

	@PostMapping(value = "/schema", consumes = "application/json")
	@ResponseStatus(code = HttpStatus.CREATED)
	SchemaInfo saveSchemaJson(@RequestBody SchemaSave schema) throws JsonProcessingException;

	@PostMapping(value = "/schema", consumes = "application/x-yaml")
	@ResponseStatus(code = HttpStatus.CREATED)
	SchemaInfo saveSchemaYaml(@RequestBody SchemaSave schema) throws JsonProcessingException;

	@GetMapping(value = "/schema/{subtype}/{namespace}/{name}/connectors")
	@ResponseStatus(code = HttpStatus.OK)
	List<JoyceSchemaMetadataExtraConnector> getConnectors(
			@PathVariable String subtype,
			@PathVariable String namespace,
			@PathVariable String name
	);

	@GetMapping(value = "/schema/{subtype}/{namespace}/{name}/connectors/{connector}/status")
	@ResponseStatus(code = HttpStatus.OK)
	JsonNode getConnectorStatus(
			@PathVariable String subtype,
			@PathVariable String namespace,
			@PathVariable String name,
			@PathVariable String connector
	);

	@PostMapping(value = "/schema/{subtype}/{namespace}/{name}/connectors/{connector}/restart")
	@ResponseStatus(code = HttpStatus.OK)
	Boolean restartConnector(
			@PathVariable String subtype,
			@PathVariable String namespace,
			@PathVariable String name,
			@PathVariable String connector
	);

	@PutMapping(value = "/schema/{subtype}/{namespace}/{name}/connectors/{connector}/pause")
	@ResponseStatus(code = HttpStatus.OK)
	Boolean pauseConnector(
			@PathVariable String subtype,
			@PathVariable String namespace,
			@PathVariable String name,
			@PathVariable String connector
	);

	@PutMapping(value = "/schema/{subtype}/{namespace}/{name}/connectors/{connector}/resume")
	@ResponseStatus(code = HttpStatus.OK)
	Boolean resumeConnector(
			@PathVariable String subtype,
			@PathVariable String namespace,
			@PathVariable String name,
			@PathVariable String connector
	);

	@PostMapping(value = "/schema/{subtype}/{namespace}/{name}/connectors/{connector}/tasks/{task}/restart")
	@ResponseStatus(code = HttpStatus.OK)
	Boolean restartConnectorTask(
			@PathVariable String subtype,
			@PathVariable String namespace,
			@PathVariable String name,
			@PathVariable String connector,
			@PathVariable String task
	);

	default JoyceURI.Subtype computeSubtype(String subtype) {
		return JoyceURI.Subtype.get(subtype)
				.orElseThrow(
						() -> new InvalidMetadataException("Subtype is not valid: " + subtype)
				);
	}
}
