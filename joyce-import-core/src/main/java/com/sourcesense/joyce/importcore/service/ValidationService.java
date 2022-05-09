package com.sourcesense.joyce.importcore.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.sourcesense.joyce.core.model.uri.JoyceSchemaURI;
import com.sourcesense.joyce.core.service.SchemaClient;
import com.sourcesense.joyce.importcore.exception.ValidationException;
import com.sourcesense.joyce.schemacore.model.dto.SchemaSave;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.Optional;
import java.util.function.Predicate;

@Service
@RequiredArgsConstructor
public class ValidationService {

	private final SchemaClient schemaClient;

	public boolean validateSchema(SchemaSave schema) {
		if(! this.isParentSchemaPresent(schema.getMetadata().getParent())) {
			this.validateSchemaUid(schema.getMetadata().getUidKey(), schema.getProperties());
		}
		return true;
	}

	protected void validateSchemaUid(String schemaUid, JsonNode properties) {
		Optional.ofNullable(schemaUid)
				.filter(Predicate.not(ObjectUtils::isEmpty))
				.map(properties::get)
				.orElseThrow(() -> new ValidationException(
						String.format("Schema uid '%s' not found in schema properties", schemaUid)
				));
	}

	protected boolean isParentSchemaPresent(JoyceSchemaURI parentURI) {
		return Optional.ofNullable(parentURI)
				.map(JoyceSchemaURI::toString)
				.flatMap(schemaClient::get)
				.isPresent();
	}
}
