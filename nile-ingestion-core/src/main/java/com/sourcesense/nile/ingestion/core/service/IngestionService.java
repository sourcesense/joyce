package com.sourcesense.nile.ingestion.core.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sourcesense.nile.ingestion.core.errors.SchemaNotFoundException;
import com.sourcesense.nile.ingestion.core.dao.Dao;
import com.sourcesense.nile.ingestion.core.model.SchemaEntity;
import com.sourcesense.nile.schemaengine.dto.ProcessResult;
import com.sourcesense.nile.schemaengine.service.SchemaEngine;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class IngestionService {
	final private SchemaEngine schemaEngine;
	final private Dao<SchemaEntity> schemaEntityDao;

	public Map processSchema(String schema, Map document) throws JsonProcessingException {
		Optional<SchemaEntity> schemaEntity = schemaEntityDao.get(schema);
		if(schemaEntity.isEmpty()){
			throw new SchemaNotFoundException(String.format("Schema %s does not exists", schema));
		}
		ProcessResult node = schemaEngine.process(schemaEntity.get().getSchema(), document);

		Map result = new HashMap<>();
		node.getMetadata().ifPresent(metadata -> {
			result.put("metadata", metadata.getAll());
		});
		result.put("result", node.getJson());

		return result;
	}
}
