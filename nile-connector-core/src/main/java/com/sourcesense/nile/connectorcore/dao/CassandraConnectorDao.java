package com.sourcesense.nile.connectorcore.dao;

import com.sourcesense.nile.connectorcore.dto.DataInfo;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public abstract class CassandraConnectorDao<T extends DataInfo> implements ConnectorDao<T>{

    @Override
    public Optional<T> get(String id) {
        return Optional.empty();
    }

    @Override
    public List<T> getAll() {
        return null;
    }

    @Override
    public T save(T dataInfo) {
        return null;
    }

    @Override
    public List<T> saveAll(List<T> dataInfos) {
        return null;
    }

    @Override
    public void delete(T dataInfo) {

    }
}
