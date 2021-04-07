package com.sourcesense.nile.core.dao;

import com.sourcesense.nile.core.model.SchemaEntity;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@Profile("cassandra")
public class CassandraSchemaDao implements Dao<SchemaEntity> {
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
}
