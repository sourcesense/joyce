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

package com.sourcesense.joyce.schemacore.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.sourcesense.joyce.core.model.entity.JoyceSchema;
import com.sourcesense.joyce.schemacore.model.dto.SchemaInfo;
import com.sourcesense.joyce.schemacore.model.dto.SchemaSave;
import com.sourcesense.joyce.schemacore.model.dto.SchemaShort;
import com.sourcesense.joyce.core.model.entity.JoyceSchemaMetadataExtraConnector;
import com.sourcesense.joyce.core.model.entity.SchemaEntity;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping(value = "/api")
@Tag(name = "Schema API", description = "Schema	 Management API")
public interface SchemaRestApi {

	@GetMapping(value = "/schema", produces = "application/json; charset=utf-8")
	@ResponseStatus(code = HttpStatus.OK)
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
	List<JoyceSchema> getAllSchemas(
			@RequestParam(defaultValue = "false", name = "full_schema") Boolean fullSchema,
			@RequestParam(defaultValue = "false", name = "root_only") Boolean rootOnly
	);

	@GetMapping(value = "/schema/{domain}/{product}", produces = "application/json; charset=utf-8")
	@ResponseStatus(code = HttpStatus.OK)
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
	List<JoyceSchema> getAllSchemasForDomainAndProduct(
			@PathVariable String domain,
			@PathVariable String product,
			@RequestParam(defaultValue = "false", name = "full_schema") Boolean fullSchema,
			@RequestParam(defaultValue = "false", name = "root_only") Boolean rootOnly
	);

	@GetMapping(value = "/schema/{domain}/{product}/{name}", produces = "application/json; charset=utf-8")
	@ResponseStatus(code = HttpStatus.OK)
	SchemaEntity getSchema(
			@PathVariable String domain,
			@PathVariable String product,
			@PathVariable String name
	);

	@DeleteMapping("/schema/{domain}/{product}/{name}")
	@ResponseStatus(code = HttpStatus.ACCEPTED)
	SchemaInfo deleteSchema(
			@PathVariable String domain,
			@PathVariable String product,
			@PathVariable String name
	);

	@PostMapping(value = "/schema", consumes = "application/json")
	@ResponseStatus(code = HttpStatus.CREATED)
	SchemaInfo saveSchemaJson(@RequestBody SchemaSave schema) throws JsonProcessingException;

	@PostMapping(value = "/schema", consumes = "application/x-yaml")
	@ResponseStatus(code = HttpStatus.CREATED)
	SchemaInfo saveSchemaYaml(@RequestBody SchemaSave schema) throws JsonProcessingException;

	@GetMapping(value = "/schema/{domain}/{product}/{name}/connectors")
	@ResponseStatus(code = HttpStatus.OK)
	List<JoyceSchemaMetadataExtraConnector> getConnectors(
			@PathVariable String domain,
			@PathVariable String product,
			@PathVariable String name
	);

	@GetMapping(value = "/schema/{domain}/{product}/{name}/connectors/{connector}/status")
	@ResponseStatus(code = HttpStatus.OK)
	JsonNode getConnectorStatus(
			@PathVariable String domain,
			@PathVariable String product,
			@PathVariable String name,
			@PathVariable String connector
	);

	@PostMapping(value = "/schema/{domain}/{product}/{name}/connectors/{connector}/restart")
	@ResponseStatus(code = HttpStatus.OK)
	Boolean restartConnector(
			@PathVariable String domain,
			@PathVariable String product,
			@PathVariable String name,
			@PathVariable String connector
	);

	@PutMapping(value = "/schema/{domain}/{product}/{name}/connectors/{connector}/pause")
	@ResponseStatus(code = HttpStatus.OK)
	Boolean pauseConnector(
			@PathVariable String domain,
			@PathVariable String product,
			@PathVariable String name,
			@PathVariable String connector
	);

	@PutMapping(value = "/schema/{domain}/{product}/{name}/connectors/{connector}/resume")
	@ResponseStatus(code = HttpStatus.OK)
	Boolean resumeConnector(
			@PathVariable String domain,
			@PathVariable String product,
			@PathVariable String name,
			@PathVariable String connector
	);

	@PostMapping(value = "/schema/{domain}/{product}/{name}/connectors/{connector}/tasks/{task}/restart")
	@ResponseStatus(code = HttpStatus.OK)
	Boolean restartConnectorTask(
			@PathVariable String domain,
			@PathVariable String product,
			@PathVariable String name,
			@PathVariable String connector,
			@PathVariable String task
	);
}
