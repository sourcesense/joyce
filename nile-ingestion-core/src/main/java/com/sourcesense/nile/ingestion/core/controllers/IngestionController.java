package com.sourcesense.nile.ingestion.core.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sourcesense.nile.ingestion.core.api.IngestionApi;
import com.sourcesense.nile.ingestion.core.service.IngestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class IngestionController implements IngestionApi {
	final private IngestionService ingestionService;

	@Override
	public Boolean ingestDocument(String schema, Map document) throws JsonProcessingException {
		return ingestionService.ingest(schema, document);
	}

	@Override
	public Map testDocumentIngestion(String schema, Map document) throws JsonProcessingException {
		return ingestionService.processSchema(schema, document);
	}
}
