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

package com.sourcesense.nile.importcore.api;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RequestMapping( value = "/api/import")
@Tag(name = "Nile Import API", description = "Nile Import API")
public interface ImportApi {

	@PostMapping(value = "/{schemaId}", produces = "application/json; charset=utf-8")
	@ResponseStatus(code = HttpStatus.OK)
	Boolean importDocument(@PathVariable String schemaId, @RequestBody ObjectNode document ) throws JsonProcessingException;

	@PostMapping(value = "/{schemaId}/test", produces = "application/json; charset=utf-8")
	@ResponseStatus(code = HttpStatus.OK)
	JsonNode importDryRun(@PathVariable String schemaId, @RequestBody ObjectNode document ) throws JsonProcessingException;

	@DeleteMapping(value = "/{schemaId}", produces = "application/json; charset=utf-8")
	@ResponseStatus(code = HttpStatus.OK)
	Boolean removeDocument(@PathVariable String schemaId, @RequestBody ObjectNode document ) throws JsonProcessingException;
}
