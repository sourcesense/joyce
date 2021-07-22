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

package com.sourcesense.joyce.importcore.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sourcesense.joyce.core.model.SchemaEntity;
import com.sourcesense.joyce.importcore.api.ImportApi;
import com.sourcesense.joyce.importcore.service.ImportService;
import com.sourcesense.joyce.core.dto.Schema;
import com.sourcesense.joyce.core.exception.SchemaNotFoundException;
import com.sourcesense.joyce.core.model.JoyceURI;
import com.sourcesense.joyce.core.service.SchemaService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 *  Controller that reads raw messages from request body and processes them.
 * 	There are two types of action that can be executed on a message: Insert and Delete.
 * 	*/
@RestController
@RequiredArgsConstructor
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
				this.computeSingleRestRawUri(),
				document,
				this.fetchSchema(schemaId)
		);
	}

	@Override
	public Boolean importDocuments(
			String schemaId,
			MultipartFile data,
			Character columnSeparator,
			String arraySeparator) throws IOException {

				JoyceURI rawUri = this.computeBulkRestRawUri(data.getOriginalFilename());
				Schema schema = this.fetchSchema(schemaId);
				List<JsonNode> documents = importService.computeDocumentsFromFile(rawUri, data, columnSeparator, arraySeparator);
				documents.parallelStream().forEach(document -> importService.processImport(rawUri, document, schema));
				return importService.notifyBulkImportSuccess(rawUri, documents);
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
				this.computeSingleRestRawUri(),
				document,
				this.fetchSchema(schemaId)
		);
		return true;
	}



	private JoyceURI computeSingleRestRawUri() {
		String uuid = UUID.randomUUID().toString().substring(0, 6);
		long timestamp = new Date().toInstant().toEpochMilli();
		String uid = String.format("%d-%s", timestamp, uuid);
		return this.computeRawUri("single", uid);
	}

	private JoyceURI computeBulkRestRawUri(String fileName) {
		return this.computeRawUri("bulk", fileName);
	}

	private JoyceURI computeRawUri(String collection, String uid) {
		return JoyceURI.make(JoyceURI.Type.RAW, JoyceURI.Subtype.REST, collection, uid);
	}

	private Schema fetchSchema(String schemaId) {
		return Optional.ofNullable(schemaId)
				.flatMap(schemaService::findByName)
				.orElseThrow(() -> new SchemaNotFoundException(String.format("Schema %s does not exists", schemaId)));
	}
}
