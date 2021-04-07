package com.sourcesense.nile.core.dao;

import com.sourcesense.nile.core.model.SchemaEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@Profile("mongodb")
@RequiredArgsConstructor
public class MongodbSchemaDao implements Dao<SchemaEntity> {

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
