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

package com.sourcesense.joyce.importgateway.controller.rest;

import com.sourcesense.joyce.importcore.controller.AbstractSchemaRestController;
import com.sourcesense.joyce.importcore.service.ConnectorService;
import com.sourcesense.joyce.schemacore.mapper.SchemaDtoMapper;
import com.sourcesense.joyce.schemacore.service.SchemaService;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SchemaRestController extends AbstractSchemaRestController {

	public SchemaRestController(
			SchemaDtoMapper schemaMapper,
			SchemaService schemaService,
			ConnectorService connectorService) {

		super(schemaMapper, schemaService, connectorService);
	}
}
