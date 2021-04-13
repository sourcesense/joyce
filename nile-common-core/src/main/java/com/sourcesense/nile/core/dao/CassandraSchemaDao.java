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
		return null;
	}

	@Override
	public void save(SchemaEntity schemaEntity) {

	}

	@Override
	public void delete(SchemaEntity schemaEntity) {

	}

	@Override
	public List<SchemaEntity> getByName(String name) {
		return null;
	}
}
