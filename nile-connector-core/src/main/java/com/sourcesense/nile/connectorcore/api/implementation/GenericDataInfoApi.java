package com.sourcesense.nile.connectorcore.api.implementation;

import com.sourcesense.nile.connectorcore.api.DataInfoApi;
import com.sourcesense.nile.connectorcore.dao.ConnectorDao;
import com.sourcesense.nile.connectorcore.dto.DataInfo;
import com.sourcesense.nile.core.exceptions.DataInfoNotFoundException;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public abstract class GenericDataInfoApi<T extends DataInfo, D extends ConnectorDao<T>> implements DataInfoApi<T> {

    protected final D connectorDao;

    @Override
    public T getInfoBy_id(String _id) {
        return connectorDao.get(_id)
                .orElseThrow(() -> new DataInfoNotFoundException(_id));
    }

    @Override
    public List<T> getAllInfo() {
        return connectorDao.getAll();
    }

    @Override
    public void deleteInfoBy_id(String _id) {
        connectorDao.get(_id)
            .ifPresentOrElse(
                    connectorDao::delete,
                    () -> { throw new DataInfoNotFoundException(_id); }
            );
    }
}
