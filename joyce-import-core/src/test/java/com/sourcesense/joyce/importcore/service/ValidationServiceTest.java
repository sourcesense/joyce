package com.sourcesense.joyce.importcore.service;

import org.bson.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class ValidationServiceTest {

	private ValidationService validationService;

	@BeforeEach
	public void init() {
		validationService = new ValidationService();
	}

	public void shouldValidateSchema() {
			Schema
			validationService.validateSchema();
			verify(validationService, times(1)).validateSchema();
	}
}
