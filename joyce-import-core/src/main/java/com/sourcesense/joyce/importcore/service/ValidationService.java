package com.sourcesense.joyce.importcore.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.sourcesense.joyce.importcore.exception.ValidationException;
import com.sourcesense.joyce.protobuf.model.Schema;
import com.sourcesense.joyce.schemacore.model.dto.SchemaSave;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.Optional;
import java.util.function.Predicate;

@Service
public class ValidationService {

	public void validateSchema(SchemaSave schema) {
		this.validateSchemaUid(schema.getMetadata().getUidKey(), schema.getProperties());
	}

	protected void validateSchemaUid(String schemaUid, JsonNode properties) {
		Optional.ofNullable(schemaUid)
				.filter(Predicate.not(ObjectUtils::isEmpty))
				.map(properties::get)
				.orElseThrow(() -> new ValidationException(
						String.format("Schema uid '%s' not found in schema properties", schemaUid)
				));
	}
}
