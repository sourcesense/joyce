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

package com.sourcesense.nile.core.dao;

import com.sourcesense.nile.core.model.SchemaEntity;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@ConditionalOnProperty(value = "nile.schema-service.database", havingValue = "cassandra")
@Component
public class CassandraSchemaDao implements SchemaDao {
	@Override
	public Optional<SchemaEntity> get(String id) {
		return Optional.empty();
	}

	@Override
	public List<SchemaEntity> getAll() {
		throw new RuntimeException("Unimplemented method");
	}

	@Override
	public void save(SchemaEntity schemaEntity) {
		throw new RuntimeException("Unimplemented method");
	}

	@Override
	public void delete(SchemaEntity schemaEntity) {
		throw new RuntimeException("Unimplemented method");
	}

}
