package com.sourcesense.nile.ingestion.core.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sourcesense.nile.core.dto.Schema;
import com.sourcesense.nile.core.dto.SchemaSave;
import com.sourcesense.nile.core.dto.SchemaShort;
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

	@GetMapping(value = "/{name}/version", produces = "application/json; charset=utf-8")
	@ResponseStatus(code = HttpStatus.OK)
	List<SchemaShort> getSchemaWithVersions(@PathVariable String name);

	@GetMapping(value = "/{name}/version/{version}", produces = "application/json; charset=utf-8")
	@ResponseStatus(code = HttpStatus.OK)
	Schema getSchemaVersion(@PathVariable String name, @PathVariable Integer version);

	@PostMapping
	@ResponseStatus(code = HttpStatus.CREATED)
	Schema saveSchema(@RequestBody SchemaSave user) throws JsonProcessingException;

	@DeleteMapping("/{name}")
	@ResponseStatus(code = HttpStatus.NO_CONTENT)
	void deleteSchema(@PathVariable String name);
}
