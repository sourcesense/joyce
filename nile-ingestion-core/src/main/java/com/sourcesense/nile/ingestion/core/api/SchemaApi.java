package com.sourcesense.nile.ingestion.core.api;

import com.sourcesense.nile.ingestion.core.dto.Schema;
import com.sourcesense.nile.ingestion.core.dto.SchemaShort;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping( value = "/api/schema")
@Tag(name = "Schema API", description = "Schema	 Management API")
public interface SchemaApi {

	@GetMapping(produces = "application/json; charset=utf-8")
	@ResponseStatus(code = HttpStatus.OK)
	List<SchemaShort> findAll();

	@GetMapping(value = "/{id}", produces = "application/json; charset=utf-8")
	@ResponseStatus(code = HttpStatus.OK)
	Schema findById(@PathVariable String id);

	@PostMapping
	@ResponseStatus(code = HttpStatus.CREATED)
	Schema save(@RequestBody Schema user);

	@PutMapping("/{id}")
	@ResponseStatus(code = HttpStatus.OK)
	Schema update(@PathVariable String id, @RequestBody Schema user);

	@DeleteMapping("/{id}")
	@ResponseStatus(code = HttpStatus.NO_CONTENT)
	void delete(@PathVariable String id);
}
