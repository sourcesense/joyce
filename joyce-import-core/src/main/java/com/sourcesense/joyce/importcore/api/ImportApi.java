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

package com.sourcesense.joyce.importcore.api;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RequestMapping(value = "/api/import")
@Tag(name = "Joyce Import API", description = "Joyce Import API")
public interface ImportApi {

	@PostMapping(value = "/{schemaId}", produces = "application/json; charset=utf-8")
	@ResponseStatus(code = HttpStatus.OK)
	Boolean importDocument(@PathVariable String schemaId, @RequestBody ObjectNode document) throws JsonProcessingException;

	@PostMapping(
			value = "/{schemaId}",
			produces = "application/json; charset=utf-8",
			consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@ResponseStatus(code = HttpStatus.OK)
	Boolean importDocuments(
			@PathVariable String schemaId,
			@RequestPart MultipartFile data,
			@RequestParam(defaultValue = ",") Character columnSeparator,
			@RequestParam(defaultValue = ";") String arraySeparator
	) throws IOException;

	@PostMapping(value = "/{schemaId}/test", produces = "application/json; charset=utf-8")
	@ResponseStatus(code = HttpStatus.OK)
	JsonNode importDryRun(@PathVariable String schemaId, @RequestBody ObjectNode document) throws JsonProcessingException;

	@DeleteMapping(value = "/{schemaId}", produces = "application/json; charset=utf-8")
	@ResponseStatus(code = HttpStatus.OK)
	Boolean removeDocument(@PathVariable String schemaId, @RequestBody ObjectNode document) throws JsonProcessingException;
}
