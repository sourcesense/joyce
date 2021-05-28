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
import com.sourcesense.joyce.importcore.api.ImportApi;
import com.sourcesense.joyce.importcore.service.ImportService;
import com.sourcesense.joyce.core.dto.Schema;
import com.sourcesense.joyce.core.exception.SchemaNotFoundException;
import com.sourcesense.joyce.core.model.JoyceURI;
import com.sourcesense.joyce.core.service.SchemaService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class ImportController implements ImportApi {

	final private ImportService importService;
	final private SchemaService schemaService;

	@Override
	public Boolean importDocument(String schemaId, ObjectNode document) {
		return importService.processImport(
				this.computeRawUri(),
				document,
				this.fetchSchema(schemaId)
		);
	}

	@Override
	public JsonNode importDryRun(String schemaId, ObjectNode document) {
		return importService.importDryRun(document, this.fetchSchema(schemaId));
	}

	@Override
	public Boolean removeDocument(String schemaId, ObjectNode document) {
		importService.removeDocument(
				this.computeRawUri(),
				document,
				this.fetchSchema(schemaId)
		);
		return true;
	}

	private JoyceURI computeRawUri() {
		String uuid = UUID.randomUUID().toString().substring(0, 6);
		long timestamp = new Date().toInstant().toEpochMilli();
		String uid = String.format("%d-%s", timestamp, uuid);
		return JoyceURI.make(JoyceURI.Type.RAW, JoyceURI.Subtype.OTHER, "rest", uid);
	}

	private Schema fetchSchema(String schemaId) {
		return Optional.ofNullable(schemaId)
				.flatMap(schemaService::findByName)
				.orElseThrow(() -> new SchemaNotFoundException(String.format("Schema %s does not exists", schemaId)));
	}
}
