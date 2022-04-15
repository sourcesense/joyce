package com.sourcesense.joyce.importcore.service;

import com.sourcesense.joyce.core.model.entity.SchemaEntity;
import com.sourcesense.joyce.core.service.SchemaClient;
import com.sourcesense.joyce.importcore.exception.ValidationException;
import com.sourcesense.joyce.importcore.test.TestUtility;
import com.sourcesense.joyce.schemacore.model.dto.SchemaSave;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ValidationServiceTest implements TestUtility {

	@Mock
	private SchemaClient schemaClient;

	private ValidationService validationService;

	private static final String PARENT_SCHEMA_URI = "joyce:content:test:default:parent:schema";

	@BeforeEach
	public void init() {
		validationService = new ValidationService(schemaClient);
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
	public void shouldSkipUidValidationIfParentIsPresent() throws IOException, URISyntaxException {
		SchemaSave schema = this.computeResourceAsObject("schema/validation/04.json", SchemaSave.class);

		when(schemaClient.get(PARENT_SCHEMA_URI)).thenReturn(Optional.of(new SchemaEntity()));

		assertTrue(validationService.validateSchema(schema));
	}

	private void shouldThrowValidationException(String path) throws IOException, URISyntaxException {
		SchemaSave schema = this.computeResourceAsObject(path, SchemaSave.class);
		assertThrows(
				ValidationException.class,
				() -> validationService.validateSchema(schema)
		);
	}
}
