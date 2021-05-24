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

package com.sourcesense.nile.importcore.consumer;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sourcesense.nile.core.dto.Schema;
import com.sourcesense.nile.core.enumeration.ImportAction;
import com.sourcesense.nile.core.enumeration.KafkaCustomHeaders;
import com.sourcesense.nile.core.exception.handler.CustomExceptionHandler;
import com.sourcesense.nile.core.model.NileURI;
import com.sourcesense.nile.importcore.service.ImportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImportConsumer {

	final private ImportService importService;
	final private CustomExceptionHandler customExceptionHandler;

	@KafkaListener(topics = "${nile.kafka.import-topic:import}")
	public void consumeMessage(
			@Payload ObjectNode message,
			@Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) String messageKey,
			@Headers Map<String, String> headers) {

		try {
			NileURI rawURI = importService.computeRawURI(messageKey, headers);
			NileURI schemaURI = importService.computeValidSchemaUri(messageKey, headers, rawURI);
			Schema schema = importService.computeSchema(schemaURI);

			//TODO: understand how to deal with deletion with kafka connect ingested
			String action = headers.getOrDefault(KafkaCustomHeaders.MESSAGE_ACTION, ImportAction.INSERT.name());

			switch (ImportAction.valueOf(action)) {
				case DELETE:
					importService.removeDocument(rawURI, schema);
					break;
				case INSERT:
					importService.processImport(rawURI, message, schema);
					break;
			}
		} catch (Exception exception) {
			customExceptionHandler.handleException(exception);
		}
	}
}
