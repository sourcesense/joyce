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

package com.sourcesense.nile.importcore.service;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sourcesense.nile.core.dto.Schema;
import com.sourcesense.nile.core.enumeration.ImportAction;
import com.sourcesense.nile.core.exception.InvalidNileUriException;
import com.sourcesense.nile.core.model.NileURI;
import com.sourcesense.nile.core.service.SchemaService;
import com.sourcesense.nile.core.exception.SchemaNotFoundException;
import com.sourcesense.nile.core.enumeration.KafkaCustomHeaders;
import com.sourcesense.nile.importcore.exception.ImportException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImportConsumer {
	final private ImportService importService;
	final private SchemaService schemaService;
	@KafkaListener(topics = "${nile.kafka.import-topic:import}")
	public void consumeMessage(@Payload ObjectNode message,
														 @Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) String messageKey,
															@Headers Map<String, String> headers) {
		try {

			if(headers.get(KafkaCustomHeaders.IMPORT_SCHEMA) == null){
				throw new ImportException(String.format("Missing %s header in message", KafkaCustomHeaders.IMPORT_SCHEMA));
			}
			Optional<NileURI> uri = NileURI.createURI(headers.get(KafkaCustomHeaders.IMPORT_SCHEMA));
			if (uri.isEmpty() || !uri.get().getSubtype().equals(NileURI.Subtype.IMPORT)){
				throw new InvalidNileUriException(String.format("Schema %s is not a valid schema uri", headers.get(KafkaCustomHeaders.IMPORT_SCHEMA)));
			}

			Optional<Schema> schema = schemaService.findByName(uri.get().getCollection());
			if(schema.isEmpty()){
				throw new SchemaNotFoundException(String.format("Schema %s does not exists", headers.get(KafkaCustomHeaders.IMPORT_SCHEMA)));
			}

			ImportAction action = ImportAction.valueOf(headers.getOrDefault(KafkaCustomHeaders.MESSAGE_ACTION, ImportAction.INSERT.name()));
			switch (action){
				case DELETE:
					importService.removeDocument(schema.get(), messageKey);
					break;
				case INSERT:
					importService.processImport(schema.get(), message, messageKey);
					break;
			}


		} catch (Exception e) {
			//TODO: forward event to notification engine
			log.error("Cannot processImport message with key: {} error: {}", messageKey, e.getMessage());
		}
	}
}
