package com.sourcesense.nile.ingestion.core.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sourcesense.nile.ingestion.core.api.SchemaApi;
import com.sourcesense.nile.ingestion.core.dto.Schema;
import com.sourcesense.nile.ingestion.core.dto.SchemaSave;
import com.sourcesense.nile.ingestion.core.dto.SchemaShort;
import com.sourcesense.nile.ingestion.core.errors.SchemaNotFoundException;
import com.sourcesense.nile.ingestion.core.service.SchemaService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class SchemaApiController implements SchemaApi {
	final protected SchemaService schemaService;

	@Override
	public List<SchemaShort> findAll() {
		return schemaService.findAll();
	}

	@Override
	public Schema findById(String id) {
		Optional<Schema> schema = schemaService.findById(id);
		if (schema.isEmpty()){
			throw new SchemaNotFoundException(String.format("Schema [%s] does not exists", id));
		}
		return schema.get();
	}

	@Override
	public Schema save(SchemaSave schema) throws JsonProcessingException {
		Schema saved = schemaService.save(schema);
		return saved;
	}

	@Override
	public void delete(String id) {
		schemaService.delete(id);
	}
}
