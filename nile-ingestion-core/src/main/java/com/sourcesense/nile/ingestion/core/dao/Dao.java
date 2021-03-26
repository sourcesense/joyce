package com.sourcesense.nile.ingestion.core.dao;

import com.sourcesense.nile.ingestion.core.model.SchemaEntity;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

public interface Dao<T> {

	Optional<T> get(String id);

	List<T> getAll();

	void save(T t);

	void delete(T t);
}
