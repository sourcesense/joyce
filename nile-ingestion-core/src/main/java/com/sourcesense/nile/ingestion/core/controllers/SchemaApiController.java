package com.sourcesense.nile.ingestion.core.controllers;

import com.sourcesense.nile.ingestion.core.api.SchemaApi;
import com.sourcesense.nile.ingestion.core.dto.SchemaDto;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class SchemaApiController implements SchemaApi {
	@Override
	public List<SchemaDto> findAll() {
		return null;
	}

	@Override
	public SchemaDto findById(String id) {
		return new SchemaDto("test");
	}

	@Override
	public SchemaDto save(SchemaDto user) {
		return null;
	}

	@Override
	public SchemaDto update(String id, SchemaDto user) {
		return null;
	}

	@Override
	public void delete(String id) {

	}
}
