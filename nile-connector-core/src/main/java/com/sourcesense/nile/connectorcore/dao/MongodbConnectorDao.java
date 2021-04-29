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

package com.sourcesense.nile.connectorcore.dao;

import com.sourcesense.nile.connectorcore.configuration.DatabaseCollections;
import com.sourcesense.nile.connectorcore.dto.DataInfo;
import com.sourcesense.nile.connectorcore.repository.DataInfoMongoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public abstract class MongodbConnectorDao<T extends DataInfo, R extends DataInfoMongoRepository<T>> implements ConnectorDao<T> {

    protected final R dataInfoMongoRepository;
    protected final MongoTemplate mongoTemplate;
    protected final DatabaseCollections databaseCollections;

    @Override
    public Optional<T> get(String id) {
        return dataInfoMongoRepository.findById(id);
    }

    @Override
    public List<T> getAll() {
        return dataInfoMongoRepository.findAll();
    }

    @Override
    public T save(T dataInfo) {
        return dataInfoMongoRepository.save(dataInfo);
    }

    @Override
    public List<T> saveAll(List<T> dataInfos) {
        return dataInfoMongoRepository.saveAll(dataInfos);
    }

    @Override
    public void delete(T dataInfo) {
        dataInfoMongoRepository.delete(dataInfo);
    }
}
