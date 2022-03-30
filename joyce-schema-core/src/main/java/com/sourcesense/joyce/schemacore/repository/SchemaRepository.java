package com.sourcesense.joyce.schemacore.repository;

import com.sourcesense.joyce.schemacore.model.entity.SchemaDocument;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

@ConditionalOnProperty(value = "joyce.data.mongodb.enabled", havingValue = "true")
public interface SchemaRepository extends MongoRepository<SchemaDocument, String> {

	@Query("{'metadata.parent': null}")
	List<SchemaDocument> findAllByMetadata_ParentIsNull();

	List<SchemaDocument> findAllByMetadata_DomainAndMetadata_Product(String domain, String product);

	@Query("{'metadata.domain': ?0, 'metadata.product': ?1, 'metadata.parent': null}")
	List<SchemaDocument> findAllByMetadata_DomainAndMetadata_ProductAndMetadata_ParentIsNull(String domain, String product);

	@Query("{'metadata.extra.reports': {'$exists': true, '$ne': []}}")
	List<SchemaDocument> findAllByReportsIsNotEmpty();
}
