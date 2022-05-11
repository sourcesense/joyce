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

package com.sourcesense.joyce.importcore.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sourcesense.joyce.core.enumeration.JoyceAction;
import com.sourcesense.joyce.core.enumeration.JoyceSchemaType;
import com.sourcesense.joyce.core.exception.handler.CustomExceptionHandler;
import com.sourcesense.joyce.core.model.entity.JoyceKafkaKey;
import com.sourcesense.joyce.core.model.entity.JoyceKafkaKeyDefaultMetadata;
import com.sourcesense.joyce.core.model.entity.JoyceSchemaMetadata;
import com.sourcesense.joyce.core.model.entity.SchemaEntity;
import com.sourcesense.joyce.core.model.uri.JoyceSchemaURI;
import com.sourcesense.joyce.core.model.uri.JoyceSourceURI;
import com.sourcesense.joyce.core.model.uri.JoyceURIFactory;
import com.sourcesense.joyce.importcore.consumer.ImportConsumer;
import com.sourcesense.joyce.importcore.test.TestUtility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ImportConsumerTest implements TestUtility {

	// Mocked components
	@Mock
	private ImportService importService;

	@Mock
	private CustomExceptionHandler customExceptionHandler;

	// Subject under test
	private ImportConsumer importConsumer;

	// CONSTANTS
	private static final String SCHEMA_URI = "joyce:content:test:default:user:schema";
	private static final String SOURCE_URI = "joyce:content:test:default:user:src:rest:single:666";

	private static final JoyceSchemaURI JOYCE_SCHEMA_URI = JoyceURIFactory.getInstance().createURIOrElseThrow(SCHEMA_URI, JoyceSchemaURI.class);
	private static final JoyceSourceURI JOYCE_SOURCE_URI = JoyceURIFactory.getInstance().createURIOrElseThrow(SOURCE_URI, JoyceSourceURI.class);

	@BeforeEach
	void init() {
		importConsumer = new ImportConsumer(importService, customExceptionHandler);
	}

	@Test
	void testActionInsert() throws JsonProcessingException {

		// mocking and stubbing data for test execution
		ObjectNode message = jsonMapper.createObjectNode();
		String kafkaKey = jsonMapper.createObjectNode()
				.put("uri", JOYCE_SOURCE_URI.toString())
				.put("action", JoyceAction.INSERT.name())
				.toString();

		JoyceKafkaKey<JoyceSourceURI, JoyceKafkaKeyDefaultMetadata> joyceKafkaKey =  JoyceKafkaKey
				.<JoyceSourceURI, JoyceKafkaKeyDefaultMetadata>builder()
				.uri(JOYCE_SOURCE_URI)
				.action(JoyceAction.INSERT)
				.build();

		SchemaEntity schema = this.computeSchemaEntity();

		when(importService.computeJoyceKafkaKey(kafkaKey)).thenReturn(joyceKafkaKey);
		when(importService.computeSchema(JOYCE_SOURCE_URI)).thenReturn(schema);

		//  Subject under test
		importConsumer.consumeMessage(message, kafkaKey);

		// asserts
		verify(importService, times(1)).processImport(any(), any(), any());
	}

	@Test
	void testActionDelete() throws JsonProcessingException {

		// mocking and stubbing data for test execution
		ObjectNode message = new ObjectNode(JsonNodeFactory.instance);
		String kafkaKey = jsonMapper.createObjectNode()
				.put("uri", JOYCE_SOURCE_URI.toString())
				.put("action", JoyceAction.DELETE.name())
				.toString();

		JoyceKafkaKey<JoyceSourceURI, JoyceKafkaKeyDefaultMetadata> joyceKafkaKey =  JoyceKafkaKey
				.<JoyceSourceURI, JoyceKafkaKeyDefaultMetadata>builder()
				.uri(JOYCE_SOURCE_URI)
				.action(JoyceAction.DELETE)
				.build();

		SchemaEntity schema = this.computeSchemaEntity();

		when(importService.computeJoyceKafkaKey(kafkaKey)).thenReturn(joyceKafkaKey);
		when(importService.computeSchema(JOYCE_SOURCE_URI)).thenReturn(schema);

		//  Subject under test
		importConsumer.consumeMessage(message, kafkaKey);

		// asserts
		verify(importService, times(1)).removeDocument(any(), any());
	}


	/* UTILITY METHODS */

	private SchemaEntity computeSchemaEntity() {
		SchemaEntity schemaEntity = new SchemaEntity();
		JoyceSchemaMetadata joyceSchemaMetadata = new JoyceSchemaMetadata();
		joyceSchemaMetadata.setType(JoyceSchemaType.IMPORT);
		schemaEntity.setMetadata(joyceSchemaMetadata);
		return schemaEntity;
	}
}
