package com.sourcesense.joyce.importcore.service;

import com.sourcesense.joyce.core.enumeration.JoyceSchemaType;
import com.sourcesense.joyce.core.model.uri.JoyceSchemaURI;
import com.sourcesense.joyce.core.model.uri.JoyceURIFactory;
import com.sourcesense.joyce.importcore.exception.ValidationException;
import com.sourcesense.joyce.schemacore.model.dto.SchemaSave;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

@Service
@RequiredArgsConstructor
public class ValidationService {

	public void validateSchema(SchemaSave schema) {
		this.validateMetadata(schema);
		this.validateProperties(schema);
		this.validateParent(schema);
		this.validateSchemaUid(schema);
	}

	protected void validateMetadata(SchemaSave schema) {
		if(Objects.isNull(schema.getMetadata())) {
			throw new ValidationException(String.format(
					"Impossible to save schema '%s', metadata node is null",
					this.computeSchemaURI(schema)
			));
		}
	}

	protected void validateProperties(SchemaSave schema) {
		if(Objects.isNull(schema.getProperties())) {
			throw new ValidationException(String.format(
					"Impossible to save schema '%s', properties node is null",
					this.computeSchemaURI(schema)
			));
		}
	}

	protected void validateParent(SchemaSave schema) {
		if(! JoyceSchemaType.IMPORT.equalsIgnoreCase(schema.getMetadata().getType())
				&& Objects.nonNull(schema.getMetadata().getParent())) {

			throw new ValidationException(String.format(
					"Impossible to save schema '%s'. Only schemas with '%s' type can have a parent but actual type is '%s'",
					this.computeSchemaURI(schema), JoyceSchemaType.IMPORT, schema.getMetadata().getType()
			));
		}
	}

	protected void validateSchemaUid(SchemaSave schemaSave) {
		Optional.ofNullable(schemaSave.getMetadata().getUidKey())
				.filter(Predicate.not(ObjectUtils::isEmpty))
				.map(schemaSave.getProperties()::get)
				.orElseThrow(() -> new ValidationException(String.format(
						"Schema uid '%s' not found in properties for schema '%s'",
						schemaSave.getMetadata().getUidKey(), this.computeSchemaURI(schemaSave))
				));
	}

	protected JoyceSchemaURI computeSchemaURI(SchemaSave schemaSave) {
		return JoyceURIFactory.getInstance().createSchemaURIOrElseThrow(
				schemaSave.getMetadata().getDomain(),
				schemaSave.getMetadata().getProduct(),
				schemaSave.getMetadata().getName()
		);
	}
}
