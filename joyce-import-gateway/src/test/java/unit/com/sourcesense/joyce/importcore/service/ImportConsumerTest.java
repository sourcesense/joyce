/*
 * Copyright 2021 Sourcesense Spa
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package unit.com.sourcesense.joyce.importcore.service;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sourcesense.joyce.core.enumeration.ImportAction;
import com.sourcesense.joyce.core.enumeration.KafkaCustomHeaders;
import com.sourcesense.joyce.core.exception.handler.CustomExceptionHandler;
import com.sourcesense.joyce.importcore.consumer.ImportConsumer;
import com.sourcesense.joyce.importcore.service.ImportService;
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
	private static final String MESSAGE_KEY = "joyce://raw/other/user/1";
	private static final String IMPORT_SCHEMA = "joyce://schema/import/user";

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
