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

package com.sourcesense.nile.importcore.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sourcesense.nile.core.dto.Schema;
import com.sourcesense.nile.core.exception.SchemaNotFoundException;
import com.sourcesense.nile.core.model.NileURI;
import com.sourcesense.nile.core.service.SchemaService;
import com.sourcesense.nile.importcore.api.ImportApi;
import com.sourcesense.nile.importcore.service.ImportService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
/**
 *  Controller that reads raw messages from request body and processes them.
 * 	There are two types of action that can be executed on a message: Insert and Delete.
 * 	*/
public class ImportController implements ImportApi {

	final private ImportService importService;
	final private SchemaService schemaService;

	/**
	 * Insert raw message using schema found in the endpoint path.
	 *
	 * @param schemaId The key of the schema
	 * @param document The payload of the message
	 * @return true if the operation succeed
	 */
	@Override
	public Boolean importDocument(String schemaId, ObjectNode document) {
		return importService.processImport(
				this.computeRawUri(),
				document,
				this.fetchSchema(schemaId)
		);
	}

	/**
	 * Test endpoint that simulates an insert without posting a message on Kafka
	 *
	 * @param schemaId The key of the schema
	 * @param document The payload of the message
	 * @return Processed message
	 */
	@Override
	public JsonNode importDryRun(String schemaId, ObjectNode document) {
		return importService.importDryRun(document, this.fetchSchema(schemaId));
	}

	/**
	 * Deletes raw message using schema found in the endpoint path.
	 *
	 * @param schemaId The key of the schema
	 * @param document The payload of the message
	 * @return true if the operation succeed
	 */
	@Override
	public Boolean removeDocument(String schemaId, ObjectNode document) {
		importService.removeDocument(
				this.computeRawUri(),
				document,
				this.fetchSchema(schemaId)
		);
		return true;
	}

	private NileURI computeRawUri() {
		String uuid = UUID.randomUUID().toString().substring(0, 6);
		long timestamp = new Date().toInstant().toEpochMilli();
		String uid = String.format("%d-%s", timestamp, uuid);
		return NileURI.make(NileURI.Type.RAW, NileURI.Subtype.OTHER, "rest", uid);
	}

	private Schema fetchSchema(String schemaId) {
		return Optional.ofNullable(schemaId)
				.flatMap(schemaService::findByName)
				.orElseThrow(() -> new SchemaNotFoundException(String.format("Schema %s does not exists", schemaId)));
	}
}
