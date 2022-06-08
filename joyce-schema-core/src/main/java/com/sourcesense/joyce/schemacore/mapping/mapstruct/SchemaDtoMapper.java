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

import com.sourcesense.joyce.core.model.entity.JoyceSchema;
import com.sourcesense.joyce.core.model.entity.SchemaEntity;
import com.sourcesense.joyce.schemacore.model.dto.SchemaSave;
import com.sourcesense.joyce.schemacore.model.dto.SchemaShort;
import org.mapstruct.Mapper;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;


@Mapper
public abstract class SchemaDtoMapper {


	public abstract SchemaShort entityToDtoShort(SchemaEntity entity);

	public abstract SchemaEntity dtoSaveToEntity(SchemaSave dto);

	public List<JoyceSchema> entitiesToDtoShortIfFullSchema(List<SchemaEntity> schemas, Boolean fullSchema) {
		return schemas.stream()
				.map(fullSchema ? Function.identity() : this::entityToDtoShort)
				.collect(Collectors.toList());
	}
}
