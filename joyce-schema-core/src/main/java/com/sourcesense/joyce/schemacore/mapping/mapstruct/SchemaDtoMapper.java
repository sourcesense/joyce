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

package com.sourcesense.joyce.schemacore.mapping.mapstruct;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sourcesense.joyce.core.model.entity.SchemaEntity;
import com.sourcesense.joyce.core.model.entity.JoyceSchema;
import com.sourcesense.joyce.core.model.uri.JoyceSchemaURI;
import com.sourcesense.joyce.core.model.uri.JoyceURIFactory;
import com.sourcesense.joyce.schemacore.model.dto.SchemaSave;
import com.sourcesense.joyce.schemacore.model.dto.SchemaShort;
import com.sourcesense.joyce.schemacore.model.entity.SchemaDocument;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;


@Mapper
public abstract class SchemaDtoMapper {

	protected ObjectMapper jsonMapper;

	@Autowired
	private void setJsonMapper(ObjectMapper jsonMapper) {
		this.jsonMapper = jsonMapper;
	}

	public abstract SchemaShort toDtoShort(SchemaEntity entity);

	public abstract SchemaSave toDtoSave(SchemaEntity entity);

	public abstract SchemaEntity toEntity(SchemaSave dto);

	@Mapping(target = "apply", source = "apply", qualifiedByName = "stringToJsonNode")
	@Mapping(target = "properties", source = "properties", qualifiedByName = "stringToJsonNode")
	@Mapping(target = "metadata.data", source = "metadata.data", qualifiedByName = "stringToJsonNode")
	public abstract SchemaEntity entityFromDocument(SchemaDocument document);

	public JoyceSchemaURI joyceSchemaURIFromString(String stringUri) {
		return JoyceURIFactory.getInstance().createURIOrElseThrow(stringUri, JoyceSchemaURI.class);
	}

	@Named("stringToJsonNode")
	public JsonNode stringToJsonNode(String string) throws JsonProcessingException {
		return jsonMapper.readTree(string);
	}

	@Mapping(target = "apply", source = "apply", qualifiedByName = "jsonNodeToString")
	@Mapping(target = "properties", source = "properties", qualifiedByName = "jsonNodeToString")
	@Mapping(target = "metadata.data", source = "metadata.data", qualifiedByName = "jsonNodeToString")
	public abstract SchemaDocument documentFromEntity(SchemaEntity entity);

	public abstract List<SchemaEntity> entitiesFromDocuments(List<SchemaDocument> documents);

	public String joyceSchemaURIToString(JoyceSchemaURI joyceSchemaURI) {
		return joyceSchemaURI.toString();
	}

	@Named("jsonNodeToString")
	public String jsonNodeToString(JsonNode node) throws JsonProcessingException {
		return jsonMapper.writeValueAsString(node);
	}

	public List<JoyceSchema> entitiesToShortIfFullSchema(List<SchemaEntity> schemas, Boolean fullSchema) {
		return schemas.stream()
				.map(fullSchema ? Function.identity() : this::toDtoShort)
				.collect(Collectors.toList());
	}
}
