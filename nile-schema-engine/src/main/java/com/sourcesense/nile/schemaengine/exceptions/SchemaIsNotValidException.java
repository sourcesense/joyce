package com.sourcesense.nile.schemaengine.exceptions;

import com.networknt.schema.ValidationMessage;
import com.networknt.schema.ValidationResult;

import java.util.stream.Collectors;

public class SchemaIsNotValidException extends NileSchemaEngineException {
	public ValidationResult getValidationResult() {
		return validationResult;
	}

	private ValidationResult validationResult;

	public SchemaIsNotValidException(ValidationResult validation) {
		super(validation.getValidationMessages().stream().map(ValidationMessage::getMessage).collect(Collectors.joining()));
		this.validationResult = validation;
	}
}
