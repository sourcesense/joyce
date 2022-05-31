package com.sourcesense.joyce.importcore.service;

import com.sourcesense.joyce.importcore.exception.ValidationException;
import com.sourcesense.joyce.importcore.test.ImportCoreJoyceTest;
import com.sourcesense.joyce.schemacore.model.dto.SchemaSave;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.net.URISyntaxException;

import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
public class ValidationServiceTest extends ImportCoreJoyceTest {

	private ValidationService validationService;

	@BeforeEach
	public void init() {
		validationService = new ValidationService();
	}

	@Test
	public void shouldValidateSchemaWithParent() throws IOException, URISyntaxException {
		SchemaSave schema = this.computeResourceAsObject("schema/validation/00.json", SchemaSave.class);
		validationService.validateSchema(schema);
	}

	@Test
	public void shouldThrowIfMissingMetadataUid() throws IOException, URISyntaxException {
		this.shouldThrowValidationException("schema/validation/01.json");
	}

	@Test
	public void shouldThrowIfEmptyMetadataUid() throws IOException, URISyntaxException {
		this.shouldThrowValidationException("schema/validation/02.json");
	}

	@Test
	public void shouldThrowIfMissingPropertiesUid() throws IOException, URISyntaxException {
		this.shouldThrowValidationException("schema/validation/03.json");
	}

	@Test
	public void shouldThrowIfNotImportHasParent() throws IOException, URISyntaxException {
		this.shouldThrowValidationException("schema/validation/04.json");
	}

	private void shouldThrowValidationException(String path) throws IOException, URISyntaxException {
		SchemaSave schema = this.computeResourceAsObject(path, SchemaSave.class);
		assertThrows(
				ValidationException.class,
				() -> validationService.validateSchema(schema)
		);
	}
}
