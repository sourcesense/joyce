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
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@ConditionalOnProperty(value = "nile.schema-service.database", havingValue = "mongodb")
@Component
@RequiredArgsConstructor
public class MongodbSchemaDao implements SchemaDao {

	@Autowired
	private MongoTemplate mongoTemplate;

	@Override
	public Optional<SchemaEntity> get(String id) {
		Query query = Query.query(Criteria.where("_id").is(id));
		SchemaEntity entity = mongoTemplate.findOne(query, SchemaEntity.class);
		return Optional.ofNullable(entity);
	}

	@Override
	public List<SchemaEntity> getAll() {
		return mongoTemplate.findAll(SchemaEntity.class);
	}

	@Override
	public void save(SchemaEntity schemaEntity) {
		mongoTemplate.save(schemaEntity);
	}

	@Override
	public void delete(SchemaEntity schemaEntity) {
		mongoTemplate.remove(schemaEntity);
	}

}
