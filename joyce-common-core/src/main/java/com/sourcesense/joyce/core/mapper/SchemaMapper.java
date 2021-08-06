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
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sourcesense.joyce.core.dao.mongodb.SchemaDocument;
import com.sourcesense.joyce.core.dto.Schema;
import com.sourcesense.joyce.core.dto.SchemaSave;
import com.sourcesense.joyce.core.dto.SchemaShort;
import com.sourcesense.joyce.core.model.SchemaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.stereotype.Component;


@Mapper
@Component
public abstract class SchemaMapper {

	ObjectMapper mapper = new ObjectMapper();

	@Mapping(target = "schema", source = "entity")
	@Mapping(target = "name", source = "metadata.name")
	@Mapping(target = "description", source = "metadata.description")
	@Mapping(target = "development", source = "metadata.development")
	public abstract Schema toDto(SchemaEntity entity);

	@Mapping(target = "name", source = "metadata.name")
	@Mapping(target = "description", source = "metadata.description")
	@Mapping(target = "development", source = "metadata.development")
	public abstract SchemaShort toDtoShort(SchemaEntity entity);

	public abstract SchemaSave toDtoSave(SchemaEntity entity);

	public abstract SchemaEntity toEntity(SchemaSave dto);

	JsonNode schemaAsMap(SchemaEntity entity) throws JsonProcessingException {
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
}
