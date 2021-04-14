package com.sourcesense.nile.core.dao;

import com.sourcesense.nile.core.model.SchemaEntity;

import java.util.List;
import java.util.Optional;

public class ConnectorSchemaDao implements SchemaDao{

    @Override
    public Optional<SchemaEntity> get(String id) {
        return Optional.empty();
    }

    @Override
    public List<SchemaEntity> getAll() {
        return null;
    }

    @Override
    public void save(SchemaEntity t) {

    }

    @Override
    public void delete(SchemaEntity t) {

    }

    @Override
    public List<SchemaEntity> getByName(String name) {
        return null;
    }
}
