package com.sourcesense.joyce.core.dao.mongodb;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@ConditionalOnProperty(value = "joyce.schema-service.database", havingValue = "mongodb")
@Repository
public interface SchemaRepository extends MongoRepository<SchemaDocument, String> {

	@Query("{'metadata.parent': null}")
	List<SchemaDocument> findAllWhereMetadata_ParentIsNull();

	List<SchemaDocument> findAllByMetadata_SubtypeAndMetadata_Namespace(String subtype, String namespace);

	@Query("{'metadata.subtype': ?0, 'metadata.namespace': ?1, 'metadata.parent': null")
	List<SchemaDocument> findAllByMetadata_SubtypeAndMetadata_NamespaceWhereMetadata_ParentIsNull(String subtype, String namespace);

}
