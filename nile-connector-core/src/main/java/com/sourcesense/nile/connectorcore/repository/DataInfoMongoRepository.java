package com.sourcesense.nile.connectorcore.repository;

import com.sourcesense.nile.connectorcore.dto.DataInfo;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface DataInfoMongoRepository<T extends DataInfo> extends MongoRepository<T, String> {
}
