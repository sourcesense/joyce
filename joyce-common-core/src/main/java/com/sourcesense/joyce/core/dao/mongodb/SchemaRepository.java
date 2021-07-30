package com.sourcesense.joyce.core.dao.mongodb;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@ConditionalOnProperty(value = "joyce.schema-service.database", havingValue = "mongodb")
@Repository
public interface SchemaRepository extends MongoRepository<SchemaDocument, String> {
	List<SchemaDocument> findAllByMetadata_SubtypeAndMetadata_Namespace(String subtype, String namespace);
	List<SchemaDocument> findAllByMetadataSubtypeAndMetadataNamespace(String subtype, String namespace);
}
