package com.sourcesense.nile.ingestion.core.controllers;

import com.sourcesense.nile.ingestion.core.api.SchemaApi;
import com.sourcesense.nile.ingestion.core.dto.Schema;
import com.sourcesense.nile.ingestion.core.dto.SchemaShort;
import com.sourcesense.nile.ingestion.core.service.SchemaService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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
		return null;
	}

	@Override
	public Schema save(Schema schema) {
		Schema saved = schemaService.save(schema);
		return saved;
	}

	@Override
	public Schema update(String id, Schema user) {
		return null;
	}

	@Override
	public void delete(String id) {

	}
}
