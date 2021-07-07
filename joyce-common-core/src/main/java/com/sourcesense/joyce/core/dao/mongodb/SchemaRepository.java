package com.sourcesense.joyce.core.dao.mongodb;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@ConditionalOnProperty(value = "joyce.schema-service.database", havingValue = "mongodb")
@Repository
public interface SchemaRepository extends MongoRepository<SchemaDocument, String> {
}
