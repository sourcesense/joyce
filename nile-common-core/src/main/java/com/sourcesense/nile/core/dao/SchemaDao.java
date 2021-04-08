package com.sourcesense.nile.core.dao;

import com.sourcesense.nile.core.model.SchemaEntity;

import java.util.List;
import java.util.Optional;

public interface SchemaDao {

	Optional<SchemaEntity> get(String id);

	List<SchemaEntity> getAll();

	void save(SchemaEntity t);

	void delete(SchemaEntity t);

    List<SchemaEntity> getByName(String name);
}
