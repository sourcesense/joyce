package com.sourcesense.nile.ingestion.core.api;


import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RequestMapping( value = "/api/ingestion")
@Tag(name = "Ingestion API", description = "Ingestion API")
public interface IngestionApi {
	@PostMapping(value = "/{schemaId}", produces = "application/json; charset=utf-8")
	@ResponseStatus(code = HttpStatus.OK)
	Boolean ingestDocument(@PathVariable String schemaId, @RequestBody Map document ) throws JsonProcessingException;

	@PostMapping(value = "/{schemaId}/test", produces = "application/json; charset=utf-8")
	@ResponseStatus(code = HttpStatus.OK)
	Map testDocumentIngestion(@PathVariable String schemaId, @RequestBody Map document ) throws JsonProcessingException;
}
