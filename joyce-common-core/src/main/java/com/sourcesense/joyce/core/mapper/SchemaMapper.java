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

package com.sourcesense.joyce.core.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.sourcesense.joyce.schemacore.dao.mongodb.SchemaDocument;
import com.sourcesense.joyce.schemacore.dto.SchemaSave;
import com.sourcesense.joyce.schemacore.dto.SchemaShort;
import com.sourcesense.joyce.core.exception.InvalidMetadataException;
import com.sourcesense.joyce.core.exception.SchemaParsingException;
import com.sourcesense.joyce.core.model.JoyceSchemaMetadata;
import com.sourcesense.joyce.core.model.JoyceSchemaMetadataExtra;
import com.sourcesense.joyce.core.model.SchemaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;


@Mapper
@Component
public abstract class SchemaMapper {

	ObjectMapper mapper = new ObjectMapper()
			.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
			.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);

	@Mapping(target = "name", source = "metadata.name")
	@Mapping(target = "description", source = "metadata.description")
	@Mapping(target = "uidKey", source = "metadata.uidKey")
	@Mapping(target = "collection", source = "metadata.collection")
	@Mapping(target = "development", source = "metadata.development")
	@Mapping(target = "connectors", source = "metadata.connectors")
	@Mapping(target = "export", source = "metadata.export")
	public abstract SchemaShort toDtoShort(SchemaEntity entity);

	public abstract SchemaSave toDtoSave(SchemaEntity entity);

	public abstract SchemaEntity toEntity(SchemaSave dto);

	JsonNode schemaAsMap(SchemaEntity entity) {
		return mapper.convertValue(entity, JsonNode.class);
	}

	@Mapping(target = "properties", source = "document")
	public abstract SchemaEntity entityFromDocument(SchemaDocument document);

	JsonNode propertiesFromString(SchemaDocument document) throws JsonProcessingException {
		return mapper.readTree(document.getProperties());
	}

	@Mapping(target = "properties", source = "entity")
	public abstract SchemaDocument documentFromEntity(SchemaEntity entity);

	String propertiesToString(SchemaEntity entity) throws JsonProcessingException {
		return mapper.writeValueAsString(entity.getProperties());
	}

	public List<SchemaEntity> entitiesFromDocuments(List<SchemaDocument> documents) {
		return documents.stream()
				.map(this::entityFromDocument)
				.collect(Collectors.toList());
	}

	public List<?> entitiesToShortIfFullSchema(List<SchemaEntity> schemas, Boolean fullSchema) {
		return schemas.stream()
				.map(fullSchema ? Function.identity() : this::toDtoShort)
				.collect(Collectors.toList());
	}

	public <T> Optional<T> jsonToObject(JsonNode json, Class<T> clazz) {
		return Optional.ofNullable(json).map(data -> mapper.convertValue(data, clazz));
	}

	public Optional<SchemaEntity> jsonToEntity(JsonNode json) {
		return Optional.ofNullable(json).map(data -> mapper.convertValue(data, SchemaEntity.class));
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
				.map(extra -> mapper.convertValue(extra, clazz));
	}
}
