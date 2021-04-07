package com.sourcesense.nile.ingestion.core.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sourcesense.nile.ingestion.core.api.IngestionApi;
import com.sourcesense.nile.core.dto.Schema;
import com.sourcesense.nile.core.errors.SchemaNotFoundException;
import com.sourcesense.nile.ingestion.core.service.IngestionService;
import com.sourcesense.nile.core.service.SchemaService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class IngestionController implements IngestionApi {
	final private IngestionService ingestionService;
	final private SchemaService schemaService;
	@Override
	public Boolean ingestDocument(String schemaId, Map document) throws JsonProcessingException {
		Optional<Schema> schema = schemaService.findById(schemaId);
		if(schema.isEmpty()){
			throw new SchemaNotFoundException(String.format("Schema %s does not exists", schemaId));
		}
		return ingestionService.ingest(schema.get(), document);
	}

	@Override
	public Map testDocumentIngestion(String schemaId, Map document) throws JsonProcessingException {
		Optional<Schema> schema = schemaService.findById(schemaId);
		if(schema.isEmpty()){
			throw new SchemaNotFoundException(String.format("Schema %s does not exists", schemaId));
		}
		return ingestionService.processSchema(schema.get(), document);
	}
}
