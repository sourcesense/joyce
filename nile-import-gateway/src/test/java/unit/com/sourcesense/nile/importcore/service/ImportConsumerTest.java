package unit.com.sourcesense.nile.importcore.service;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sourcesense.nile.core.enumeration.ImportAction;
import com.sourcesense.nile.core.enumeration.KafkaCustomHeaders;
import com.sourcesense.nile.core.exception.handler.CustomExceptionHandler;
import com.sourcesense.nile.importcore.consumer.ImportConsumer;
import com.sourcesense.nile.importcore.service.ImportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ImportConsumerTest {

	// Mocked components
	@Mock
	private ImportService importService;

	@Mock
	private CustomExceptionHandler customExceptionHandler;

	// Subject under test
	private ImportConsumer importConsumer;

	// CONSTANTS
	private static final String MESSAGE_KEY = "nile://raw/other/user/1";
	private static final String IMPORT_SCHEMA = "nile://schema/import/user";

	@BeforeEach
	void init() {
		importConsumer = new ImportConsumer(importService, customExceptionHandler);
	}

	@Test
	void testActionInsert() {

		// mocking and stubbing data for test execution
		ObjectNode message = new ObjectNode(JsonNodeFactory.instance);
		Map<String, String> headers = computeHeaders(ImportAction.INSERT);

		//  Subject under test
		importConsumer.consumeMessage(message, MESSAGE_KEY, headers);

		// asserts
		verify(importService, times(1)).processImport(any(), any(), any());
	}

	@Test
	void testActionDelete() {

		// mocking and stubbing data for test execution
		ObjectNode message = new ObjectNode(JsonNodeFactory.instance);
		Map<String, String> headers = computeHeaders(ImportAction.DELETE);

		//  Subject under test
		importConsumer.consumeMessage(message, MESSAGE_KEY, headers);

		// asserts
		verify(importService, times(1)).removeDocument(any(), any());
	}


	/* UTILITY METHODS */
	private Map<String, String> computeHeaders(ImportAction delete) {
		return Map.of(
				KafkaCustomHeaders.MESSAGE_ACTION, delete.toString(),
				KafkaCustomHeaders.IMPORT_SCHEMA, IMPORT_SCHEMA
		);
	}
}
