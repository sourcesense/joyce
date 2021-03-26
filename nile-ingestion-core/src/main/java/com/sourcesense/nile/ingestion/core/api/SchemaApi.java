package com.sourcesense.nile.ingestion.core.api;

import com.sourcesense.nile.ingestion.core.dto.SchemaDto;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping( value = "/api/schema")
@Tag(name = "User API", description = "User Management API")
public interface SchemaApi {

	@GetMapping(produces = "application/json; charset=utf-8")
	@ResponseStatus(code = HttpStatus.OK)
	List<SchemaDto> findAll();

	@GetMapping(value = "/{id}", produces = "application/json; charset=utf-8")
	@ResponseStatus(code = HttpStatus.OK)
	SchemaDto findById(@PathVariable String id);

	@PostMapping
	@ResponseStatus(code = HttpStatus.CREATED)
	SchemaDto save(@RequestBody SchemaDto user);

	@PutMapping("/{id}")
	@ResponseStatus(code = HttpStatus.OK)
	SchemaDto update(@PathVariable String id, @RequestBody SchemaDto user);

	@DeleteMapping("/{id}")
	@ResponseStatus(code = HttpStatus.NO_CONTENT)
	void delete(@PathVariable String id);
}
