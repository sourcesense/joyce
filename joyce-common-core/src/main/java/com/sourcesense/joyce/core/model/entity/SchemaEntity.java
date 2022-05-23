/*
 *  Copyright 2021 Sourcesense Spa
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

package com.sourcesense.joyce.core.model.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.sourcesense.joyce.core.model.uri.JoyceSchemaURI;
import lombok.Data;
import org.springframework.data.annotation.Id;

import java.util.List;

@Data
public class SchemaEntity implements SchemaObject {

	@Id
	private JoyceSchemaURI uid;

	@JsonProperty("$schema")
	private String schema;
	private JoyceSchemaMetadata metadata;

	private String type;
	private List<String> required;
	private JsonNode properties;
}
