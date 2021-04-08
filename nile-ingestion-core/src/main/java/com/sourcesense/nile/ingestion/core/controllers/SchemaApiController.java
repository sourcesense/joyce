package com.sourcesense.nile.ingestion.core.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sourcesense.nile.ingestion.core.api.SchemaApi;
import com.sourcesense.nile.core.dto.Schema;
import com.sourcesense.nile.core.dto.SchemaSave;
import com.sourcesense.nile.core.dto.SchemaShort;
import com.sourcesense.nile.core.errors.SchemaNotFoundException;
import com.sourcesense.nile.core.service.SchemaService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class SchemaApiController implements SchemaApi {
	final protected SchemaService schemaService;

	@Override
	public List<SchemaShort> getAllSchema() {
		return schemaService.findAll();
	}

	@Override
	public Schema getSchema(String name) {
		Optional<Schema> schema = schemaService.findByName(name);
		if (schema.isEmpty()){
			throw new SchemaNotFoundException(String.format("Schema [%s] does not exists", name));
		}
		return schema.get();
	}

	@Override
	public List<Schema> getSchemaWithVersions(String name) {
		List<Schema> schemas = schemaService.getAllVersions(name);
		if (schemas.size() < 1){
			throw new SchemaNotFoundException(String.format("Schema [%s] does not exists", name));
		}
		return schemas;
	}

	@Override
	public Schema saveSchema(SchemaSave schema) throws JsonProcessingException {
		Schema saved = schemaService.save(schema);
		return saved;
	}

	@Override
	public void deleteSchema(String id) {
		schemaService.delete(id);
	}
}
