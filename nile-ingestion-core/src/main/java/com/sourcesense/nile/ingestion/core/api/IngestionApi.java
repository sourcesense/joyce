package com.sourcesense.nile.ingestion.core.api;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RequestMapping( value = "/api/ingestion")
@Tag(name = "Ingestion API", description = "Ingestion API")
public interface IngestionApi {
	@PostMapping(value = "/{schemaId}", produces = "application/json; charset=utf-8")
	@ResponseStatus(code = HttpStatus.OK)
	Boolean ingestDocument(@PathVariable String schemaId, @RequestHeader(value = "X-Nile-SchemaVersion", required = false) Optional<Integer> schemaVersion, @RequestBody ObjectNode document ) throws JsonProcessingException;

	@PostMapping(value = "/{schemaId}/test", produces = "application/json; charset=utf-8")
	@ResponseStatus(code = HttpStatus.OK)
	JsonNode testDocumentIngestion(@PathVariable String schemaId, @RequestHeader(value = "X-Nile-SchemaVersion", required = false) Optional<Integer> schemaVersion, @RequestBody ObjectNode document ) throws JsonProcessingException;

	@DeleteMapping(value = "/{schemaId}", produces = "application/json; charset=utf-8")
	@ResponseStatus(code = HttpStatus.OK)
	Boolean removeDocument(@PathVariable String schemaId, @RequestBody ObjectNode document ) throws JsonProcessingException;
}
