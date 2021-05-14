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

package com.sourcesense.nile.importcore.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sourcesense.nile.core.dto.Schema;
import com.sourcesense.nile.core.enumeration.NotificationEvent;
import com.sourcesense.nile.core.exception.NotificationException;
import com.sourcesense.nile.core.exception.SchemaNotFoundException;
import com.sourcesense.nile.core.service.SchemaService;
import com.sourcesense.nile.importcore.api.ImportApi;
import com.sourcesense.nile.importcore.service.ImportService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class ImportController implements ImportApi {
	final private ImportService importService;
	final private SchemaService schemaService;

	@Override
	public Boolean importDocument(String schemaId, ObjectNode document) {
		try{
			Optional<Schema> schema = schemaService.findByName(schemaId);

			if(schema.isEmpty()){
				throw new SchemaNotFoundException(String.format("Schema %s does not exists", schemaId));
			}
			return importService.processImport(schema.get(), document, null);

		}catch (Exception e){
			//TODO rifattorizzare per la gestione del content URI che dovrà essere l'identificativo dei messaggi sul log di notifiche
			throw new NotificationException(
					String.format("Cannot import document. error: %s", e.getMessage()),
					NotificationEvent.RAW_DATA_IMPORT_FAILED);
		}

	}

	@Override
	public JsonNode importDryRun(String schemaId,  ObjectNode document) {
		Optional<Schema> schema = schemaService.findByName(schemaId);

		if(schema.isEmpty()){
			throw new SchemaNotFoundException(String.format("Schema %s does not exists", schemaId));
		}
		return importService.importDryRun(schema.get(), document);
	}

    @Override
    public Boolean removeDocument(String schemaId, ObjectNode document) {
			try {
				Optional<Schema> schema = schemaService.findByName(schemaId);

				if (schema.isEmpty()) {
					throw new SchemaNotFoundException(String.format("Schema %s does not exists", schemaId));
				}
				importService.removeDocument(schema.get(), document);
				return true;
			}		catch (Exception e){
		//TODO rifattorizzare per la gestione del content URI che dovrà essere l'identificativo dei messaggi sul log di notifiche
		throw new NotificationException(
				String.format("Cannot remove document document. error: %s", e.getMessage()),
				NotificationEvent.RAW_DATA_REMOVAL_FAILED);
	}
		}
}
