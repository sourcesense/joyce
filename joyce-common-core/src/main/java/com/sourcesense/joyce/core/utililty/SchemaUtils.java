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

package com.sourcesense.joyce.core.utililty;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sourcesense.joyce.core.exception.InvalidMetadataException;
import com.sourcesense.joyce.core.exception.SchemaParsingException;
import com.sourcesense.joyce.core.model.JoyceSchemaMetadata;
import com.sourcesense.joyce.core.model.JoyceSchemaMetadataExtra;
import com.sourcesense.joyce.core.model.SchemaEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class SchemaUtils {

	private final ObjectMapper jsonMapper;

	public <T> Optional<T> jsonToObject(JsonNode json, Class<T> clazz) {
		return Optional.ofNullable(json).map(data -> jsonMapper.convertValue(data, clazz));
	}

	public Optional<SchemaEntity> jsonToEntity(JsonNode json) {
		return Optional.ofNullable(json).map(data -> jsonMapper.convertValue(data, SchemaEntity.class));
	}

	public SchemaEntity jsonToEntityOrElseThrow(JsonNode jsonNode) {
		return this.jsonToEntity(jsonNode)
				.orElseThrow(() -> new SchemaParsingException(
						"Impossible to convert schema to entity"
				));
	}

	/**
	 * //	 * Retrieves the metadata from the schema
	 * //	 *
	 * //	 * @param schema
	 * //	 * @return Schema metadata
	 * //
	 */
	public Optional<JoyceSchemaMetadata> metadataFromSchema(SchemaEntity schema) {
		return Optional.ofNullable(schema).map(SchemaEntity::getMetadata);
	}

	public JoyceSchemaMetadata metadataFromSchemaOrElseThrow(SchemaEntity schema) {
		return this.metadataFromSchema(schema)
				.orElseThrow(() -> new InvalidMetadataException(
						String.format("Schema '%s' has no metadata", schema.getUid())
				));
	}

	public <T extends JoyceSchemaMetadataExtra> Optional<T> metadataExtraFromSchema(
			SchemaEntity schema,
			Class<T> clazz) {

		return Optional.of(schema)
				.map(SchemaEntity::getMetadata)
				.map(JoyceSchemaMetadata::getExtra)
				.map(extra -> jsonMapper.convertValue(extra, clazz));
	}
}
